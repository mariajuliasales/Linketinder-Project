package com.mariajuliasales.api

import com.mariajuliasales.controller.RecruiterController
import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.service.RecruiterLikeService
import com.mariajuliasales.service.RecruiterService
import com.mariajuliasales.service.validator.RecruiterValidator
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification

import java.time.LocalDate

import static com.mariajuliasales.fixture.TestData.validEnterprise

class RecruiterServletSpec extends Specification {

	private static final String VALID_JSON = '''{
		"enterpriseId": 3, "name": "Rafael Teixeira Lopes", "email": "rafael.lopes@techsolutions.com.br",
		"password": "123456", "description": "Recrutador de tecnologia", "cpf": "301.958.476-01",
		"birthDate": "1988-09-03", "area": "TI"
	}'''

	RecruiterRepository recruiterRepository = Mock()
	EnterpriseRepository enterpriseRepository = Mock()
	ConnectionManager connectionManager = Stub()
	RecruiterServlet servlet = new RecruiterServlet(new RecruiterController(new RecruiterService(recruiterRepository,
	enterpriseRepository, new RecruiterValidator(), connectionManager, Stub(LikeRepository),
	new RecruiterLikeService(connectionManager, recruiterRepository, Stub(CandidateRepository), Stub(LikeRepository),
	Stub(MatchRepository)))))

	StringWriter body = new StringWriter()
	HttpServletResponse response = Stub() {
		getWriter() >> new PrintWriter(body)
	}
	Integer status
	Map<String, String> headers = [:]

	def setup() {
		response.setStatus(_) >> { int value -> status = value }
		response.setHeader(_, _) >> { String name, String value -> headers[name] = value }
	}

	private HttpServletRequest request(String json) {
		Stub(HttpServletRequest) {
			getReader() >> new BufferedReader(new StringReader(json))
		}
	}

	private Map responseJson() {
		new JsonSlurper().parseText(body.toString()) as Map
	}

	def "a valid JSON should save the recruiter for the enterprise and answer 201 with its location (CA06)"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)
		recruiterRepository.isEmailInUse(_) >> false

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		1 * recruiterRepository.create({ Recruiter recruiter ->
			recruiter.enterpriseId == 3 && recruiter.birthDate == LocalDate.of(1988, 9, 3)
		}) >> { Recruiter recruiter -> recruiter.id = 10; recruiter }
		status == 201
		headers.Location == '/api/recruiters/10'
		responseJson() == [id: 10, name: 'Rafael Teixeira Lopes', email: 'rafael.lopes@techsolutions.com.br',
			area: 'TI', description: 'Recrutador de tecnologia']
	}

	def "the JSON body should not contain sensitive keys (CA44)"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)
		recruiterRepository.isEmailInUse(_) >> false
		recruiterRepository.create(_) >> { Recruiter recruiter -> recruiter.id = 10; recruiter }

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		status == 201
		responseJson().keySet().intersect(['cpf', 'birthDate', 'age', 'password', 'enterpriseId']).isEmpty()
		!body.toString().contains('301.958.476-01')
		!body.toString().contains('1988-09-03')
		!body.toString().contains('123456')
	}

	def "enterpriseId missing or not a number should answer 400 without saving"() {
		when:
		servlet.doPost(request(json), response)

		then:
		0 * enterpriseRepository.findById(_)
		0 * recruiterRepository.create(_)
		status == 400
		responseJson().error == 'O campo enterpriseId é obrigatório e deve ser um número'

		where:
		json << [VALID_JSON.replace('"enterpriseId": 3,', ''), VALID_JSON.replace('"enterpriseId": 3', '"enterpriseId": "3"')]
	}

	def "a missing enterprise should answer 404 without saving (CA13)"() {
		given:
		enterpriseRepository.findById(3) >> null

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		0 * recruiterRepository.create(_)
		status == 404
		responseJson().error == 'Empresa com ID 3 não existe'
	}

	def "an e-mail already in use should answer 409 without saving (CA07)"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)
		recruiterRepository.isEmailInUse(_) >> true

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		0 * recruiterRepository.create(_)
		status == 409
		responseJson().error == 'E-mail já cadastrado'
	}

	def "an invalid CPF should answer 400 and a repeated CPF should answer 409 (CA08)"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)
		recruiterRepository.isEmailInUse(_) >> false
		recruiterRepository.create(_) >> { throw new DuplicateEntityException('CPF já cadastrado') }

		when:
		servlet.doPost(request(VALID_JSON.replace('301.958.476-01', cpf)), response)

		then:
		status == expectedStatus
		responseJson().error == message

		where:
		cpf              | expectedStatus | message
		'111.111.111-11' | 400            | 'CPF inválido'
		'301.958.476-01' | 409            | 'CPF já cadastrado'
	}

	def "malformed JSON or a date outside the ISO format should answer 400"() {
		when:
		servlet.doPost(request(json), response)

		then:
		0 * recruiterRepository.create(_)
		status == 400
		responseJson().error == 'JSON inválido'

		where:
		json << ['{"name": ', VALID_JSON.replace('1988-09-03', '03/09/1988')]
	}
}
