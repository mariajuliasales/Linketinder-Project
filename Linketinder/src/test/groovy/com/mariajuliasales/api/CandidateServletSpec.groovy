package com.mariajuliasales.api

import com.mariajuliasales.controller.CandidateController
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.validator.CandidateValidator
import com.mariajuliasales.service.validator.PersonValidator
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification

import java.time.LocalDate

class CandidateServletSpec extends Specification {

	private static final String VALID_JSON = '''{
		"name": "Sandubinha", "email": "sandubinha@email.com", "password": "123456",
		"address": {"cep": "50000-000", "city": "Recife", "state": "PE"},
		"description": "Dev mirabolante", "competences": ["Java", "Groovy"],
		"cpf": "529.982.247-25", "birthDate": "1995-05-20", "training": "Sistemas"
	}'''

	CandidateRepository candidateRepository = Mock()
	CandidateServlet servlet = new CandidateServlet(new CandidateController(
	new CandidateService(candidateRepository, new CandidateValidator(new PersonValidator()))))

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

	def "a valid JSON should save the candidate and answer 201 with its location"() {
		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		1 * candidateRepository.create({ Candidate candidate ->
			candidate.name == 'Sandubinha' &&
					candidate.address.city == 'Recife' &&
					candidate.birthDate == LocalDate.of(1995, 5, 20) &&
					candidate.competences*.name == ['Java', 'Groovy']
		}) >> { Candidate candidate -> candidate.id = 7; candidate }
		1 * response.setStatus(201)
		1 * response.setHeader('Location', '/api/candidates/7')
		responseJson().id == 7
		responseJson().birthDate == '1995-05-20'
		!responseJson().containsKey('password')
	}

	def "invalid data should answer 400 without saving"() {
		when:
		servlet.doPost(request(VALID_JSON.replace('529.982.247-25', '111.111.111-11')), response)

		then:
		0 * candidateRepository.create(_)
		1 * response.setStatus(400)
		responseJson().error == 'CPF inválido'
	}

	def "a repeated e-mail should answer 409"() {
		given:
		candidateRepository.create(_) >> { throw new DuplicateEntityException('E-mail já cadastrado') }

		when:
		servlet.doPost(request(VALID_JSON), response)

		then:
		1 * response.setStatus(409)
		responseJson().error == 'E-mail já cadastrado'
	}

	def "malformed JSON or a date outside the ISO format should answer 400"() {
		when:
		servlet.doPost(request(json), response)

		then:
		0 * candidateRepository.create(_)
		1 * response.setStatus(400)
		responseJson().error == 'JSON inválido'

		where:
		json << ['{"name": ', VALID_JSON.replace('1995-05-20', '20/05/1995')]
	}
}
