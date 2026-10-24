package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import spock.lang.Specification
import spock.lang.Unroll

import static com.mariajuliasales.fixture.TestData.validCandidate

class CandidateValidatorSpec extends Specification {

	def "valid candidate should pass"() {
		when:
		CandidateValidator.validateForCreate(validCandidate())

		then:
		noExceptionThrown()
	}

	def "candidate validation should include the common person rules"() {
		when:
		CandidateValidator.validateForCreate(validCandidate(email: "invalid"))

		then:
		ValidationException exception = thrown()
		exception.message == "E-mail inválido"
	}

	@Unroll
	def "candidate with invalid cpf '#cpf' should fail"() {
		when:
		CandidateValidator.validateForCreate(validCandidate(cpf: cpf))

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
		CandidateValidator.validateForUpdate(validCandidate([(field): value]))

		then:
		ValidationException exception = thrown()
		exception.message == message

		where:
		field       | value || message
		"birthDate" | null  || "Data de nascimento é obrigatória"
		"training"  | " "   || "Formação é obrigatória"
		"training"  | null  || "Formação é obrigatória"
	}
}
