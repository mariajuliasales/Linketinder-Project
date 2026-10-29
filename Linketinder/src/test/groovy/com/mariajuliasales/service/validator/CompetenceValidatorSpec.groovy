package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Competence
import spock.lang.Specification
import spock.lang.Unroll

class CompetenceValidatorSpec extends Specification {

	CompetenceValidator validator = new CompetenceValidator()

	@Unroll
	def "competence name '#name' should pass"() {
		when:
		validator.validateForCreate(new Competence(name))

		then:
		noExceptionThrown()

		where:
		name << [
			"Java",
			"x" * 50,
			"  Groovy  "
		]
	}

	@Unroll
	def "empty competence name '#name' should fail"() {
		when:
		validator.validateForCreate(new Competence(name))

		then:
		ValidationException exception = thrown()
		exception.message == "Nome da competência é obrigatório"

		where:
		name << [null, "", "   "]
	}

	def "competence name longer than 50 characters should fail"() {
		when:
		validator.validateForUpdate(new Competence("x" * 51))

		then:
		ValidationException exception = thrown()
		exception.message == "Nome da competência deve ter no máximo 50 caracteres"
	}
}
