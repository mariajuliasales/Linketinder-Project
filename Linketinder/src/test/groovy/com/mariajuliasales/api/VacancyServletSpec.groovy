package com.mariajuliasales.api

import com.mariajuliasales.controller.EnterpriseController
import com.mariajuliasales.controller.VacancyController
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.VacancyRepository
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import com.mariajuliasales.service.validator.VacancyValidator
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validEnterprise

class VacancyServletSpec extends Specification {

	private static final String VALID_JSON = '''{
		"enterpriseId": 3, "title": "Dev Groovy", "description": "APIs REST",
		"city": "Recife", "state": "PE", "competences": ["Groovy"]
	}'''

	EnterpriseRepository enterpriseRepository = Mock()
	VacancyRepository vacancyRepository = Mock()
	VacancyServlet servlet = new VacancyServlet(new VacancyController(
	new VacancyService(vacancyRepository, new VacancyValidator()),
	new EnterpriseService(enterpriseRepository, new EnterpriseValidator(new PersonValidator()))))

	StringWriter body = new StringWriter()
	HttpServletResponse response = Mock()

	def setup() {
		response.getWriter() >> new PrintWriter(body)
	}

	private HttpServletRequest request(String json) {
		Stub(HttpServletRequest) {
			getReader() >> new BufferedReader(new StringReader(json))
		}
	}

	private Map responseJson() {
		new JsonSlurper().parseText(body.toString()) as Map
	}

	def "a valid JSON should save the vacancy for the enterprise and answer 201"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		1 * vacancyRepository.create({ Vacancy vacancy ->
			vacancy.enterprise.id == 3 && vacancy.title == 'Dev Groovy'
		}) >> { Vacancy vacancy -> vacancy.id = 12; vacancy }
		1 * response.setStatus(201)
		1 * response.setHeader('Location', '/api/vacancies/12')
		responseJson().id == 12
		responseJson().status == 'OPEN'
	}

	def "a missing enterprise should answer 404 without saving"() {
		given:
		enterpriseRepository.findById(3) >> null

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		0 * vacancyRepository.create(_)
		1 * response.setStatus(404)
		responseJson().error == 'Empresa com ID 3 não existe'
	}

	def "enterpriseId missing or not a number should answer 400"() {
		when:
		servlet.doPost(request(json), response)

		then:
		0 * enterpriseRepository.findById(_)
		0 * vacancyRepository.create(_)
		1 * response.setStatus(400)
		responseJson().error == 'O campo enterpriseId é obrigatório e deve ser um número'

		where:
		json << ['{"title": "Dev Groovy"}', VALID_JSON.replace('"enterpriseId": 3', '"enterpriseId": "3"')]
	}

	def "invalid vacancy data should answer 400 without saving"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)

		when:
		servlet.doPost(request(VALID_JSON.replace('"description": "APIs REST",', '')), response)

		then:
		0 * vacancyRepository.create(_)
		1 * response.setStatus(400)
		responseJson().error == 'Descrição da vaga é obrigatória'
	}
}
