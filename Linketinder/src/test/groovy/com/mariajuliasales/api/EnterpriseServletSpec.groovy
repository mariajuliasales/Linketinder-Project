package com.mariajuliasales.api

import com.mariajuliasales.controller.EnterpriseController
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification

class EnterpriseServletSpec extends Specification {

	private static final String VALID_JSON = '''{
		"name": "Sanduíches S.A.", "email": "rh@sanduiches.com", "password": "123456",
		"address": {"cep": "01310-000", "city": "São Paulo", "state": "SP"},
		"description": "Lanches", "competences": ["Java"], "cnpj": "11.222.333/0001-81"
	}'''

	EnterpriseRepository enterpriseRepository = Mock()
	EnterpriseServlet servlet = new EnterpriseServlet(new EnterpriseController(
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

	def "a valid JSON should save the enterprise and answer 201 with its location"() {
		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		1 * enterpriseRepository.create({ Enterprise enterprise ->
			enterprise.name == 'Sanduíches S.A.' && enterprise.address.state == 'SP'
		}) >> { Enterprise enterprise -> enterprise.id = 3; enterprise }
		1 * response.setStatus(201)
		1 * response.setHeader('Location', '/api/enterprises/3')
		responseJson().id == 3
		responseJson().name == 'Sanduíches S.A.'
		!responseJson().containsKey('password')
	}

	def "invalid data should answer 400 without saving"() {
		when:
		servlet.doPost(request(VALID_JSON.replace('rh@sanduiches.com', 'rh.sanduiches')), response)

		then:
		0 * enterpriseRepository.create(_)
		1 * response.setStatus(400)
		responseJson().error == 'E-mail inválido'
	}

	def "a repeated CNPJ should answer 409"() {
		given:
		enterpriseRepository.create(_) >> { throw new DuplicateEntityException('CNPJ já cadastrado') }

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		1 * response.setStatus(409)
		responseJson().error == 'CNPJ já cadastrado'
	}
}
