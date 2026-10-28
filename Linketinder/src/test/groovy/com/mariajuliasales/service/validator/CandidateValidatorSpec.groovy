package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Person
import spock.lang.Specification
import spock.lang.Unroll

import static com.mariajuliasales.fixture.TestData.validCandidate

class CandidateValidatorSpec extends Specification {

	CandidateValidator validator = new CandidateValidator(new PersonValidator())

	def "valid candidate should pass"() {
		when:
		validator.validateForCreate(validCandidate())

		then:
		noExceptionThrown()
	}

	def "candidate validation should include the common person rules"() {
		when:
		validator.validateForCreate(validCandidate(email: "invalid"))

		then:
		ValidationException exception = thrown()
		exception.message == "E-mail inválido"
	}

	@Unroll
	def "candidate with invalid cpf '#cpf' should fail"() {
		when:
		validator.validateForCreate(validCandidate(cpf: cpf))

		then:
		ValidationException exception = thrown()
		exception.message == "CPF inválido"

		where:
		cpf << [
			null,
			"invalid",
			"123.456.789-00",
			"000.000.000-00"
		]
	}

	@Unroll
	def "candidate with invalid #field should fail with '#message'"() {
		when:
		validator.validateForUpdate(validCandidate([(field): value]))

		then:
		ValidationException exception = thrown()
		exception.message == message

		where:
		field       | value || message
		"birthDate" | null  || "Data de nascimento é obrigatória"
		"training"  | " "   || "Formação é obrigatória"
		"training"  | null  || "Formação é obrigatória"
	}

	def "common person rules should be delegated to the injected person validator"() {
		given:
		Validator<Person> personValidator = Mock()
		CandidateValidator candidateValidator = new CandidateValidator(personValidator)
		Candidate candidate = validCandidate()

		when:
		candidateValidator.validateForCreate(candidate)
		candidateValidator.validateForUpdate(candidate)

		then:
		1 * personValidator.validateForCreate(candidate)
		1 * personValidator.validateForUpdate(candidate)
	}
}
