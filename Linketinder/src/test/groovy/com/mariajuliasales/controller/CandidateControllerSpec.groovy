package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.dto.response.CandidateResponse
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.validator.CandidateValidator
import com.mariajuliasales.service.validator.PersonValidator
import spock.lang.Specification

import java.time.LocalDate

import static com.mariajuliasales.fixture.TestData.validCandidate

class CandidateControllerSpec extends Specification {

	CandidateRepository candidateRepository = Mock()
	CandidateController controller = new CandidateController(
	new CandidateService(candidateRepository, new CandidateValidator(new PersonValidator())))

	private static CandidateRequest request(Map changes = [:]) {
		Map values = [name: 'João Silva', email: 'joao@email.com', password: '123456', cpf: '529.982.247-25'] + changes
		new CandidateRequest(values.name, values.email, values.password,
				new AddressRequest('01310-000', null, null, null, null, 'São Paulo', 'SP', 'Brasil'),
				'Desenvolvedor Java', ['Java', 'Groovy'], values.cpf, LocalDate.of(1995, 5, 20), 'Ciência da Computação')
	}

	def "create should convert the request, save the candidate and return the response"() {
		when:
		CandidateResponse response = controller.create(request())

		then:
		1 * candidateRepository.create({ Candidate candidate ->
			candidate.name == 'João Silva' && candidate.competences*.name == ['Java', 'Groovy']
		}) >> { Candidate candidate -> candidate.id = 10; candidate }
		response.id() == 10
		response.name() == 'João Silva'
		response.competences() == ['Java', 'Groovy']
	}

	def "create should not reach the repository when the request is invalid"() {
		when:
		controller.create(request(cpf: '111.111.111-11'))

		then:
		thrown(ValidationException)
		0 * candidateRepository.create(_)
	}

	def "find all should return only anonymous profiles"() {
		given:
		candidateRepository.findAll() >> [validCandidate(id: 1), validCandidate(id: 2)]

		when:
		List<CandidateAnonymousResponse> responses = controller.findAll()

		then:
		responses*.id() == [1, 2]
		responses.every { it instanceof CandidateAnonymousResponse }
	}

	def "find by id should return the full profile with the birth date"() {
		given:
		candidateRepository.findById(1) >> validCandidate()

		expect:
		controller.findById(1).birthDate() == LocalDate.of(1995, 5, 20)
	}

	def "update should send the candidate with the given id"() {
		when:
		CandidateResponse response = controller.update(5, request(name: 'João Souza', password: ''))

		then:
		1 * candidateRepository.update({ Candidate candidate -> candidate.id == 5 && candidate.name == 'João Souza' }) >>
		{ Candidate candidate -> candidate }
		response.name() == 'João Souza'
	}

	def "missing candidates should raise a not found error"() {
		given:
		candidateRepository.findById(99) >> null
		candidateRepository.delete(99) >> false

		when:
		controller.findById(99)

		then:
		thrown(EntityNotFoundException)

		when:
		controller.delete(99)

		then:
		thrown(EntityNotFoundException)
	}
}
