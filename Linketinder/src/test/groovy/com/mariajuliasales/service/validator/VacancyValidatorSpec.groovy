package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Address
import spock.lang.Specification
import spock.lang.Unroll

import static com.mariajuliasales.fixture.TestData.validVacancy

class VacancyValidatorSpec extends Specification {

	def "valid vacancy should pass"() {
		when:
		VacancyValidator.validate(validVacancy())

		then:
		noExceptionThrown()
	}

	def "null vacancy should fail"() {
		when:
		VacancyValidator.validate(null)

		then:
		ValidationException exception = thrown()
		exception.message == "Dados da vaga não informados"
	}

	@Unroll
	def "vacancy with invalid #field '#value' should fail with '#message'"() {
		when:
		VacancyValidator.validate(validVacancy([(field): value]))

		then:
		ValidationException exception = thrown()
		exception.message == message

		where:
		field         | value                                   || message
		"title"       | null                                    || "Título da vaga é obrigatório"
		"title"       | "   "                                   || "Título da vaga é obrigatório"
		"description" | ""                                      || "Descrição da vaga é obrigatória"
		"enterprise"  | null                                    || "A vaga deve pertencer a uma empresa"
		"address"     | null                                    || "Cidade e UF da vaga são obrigatórias"
		"address"     | new Address(city: "", state: "SP")      || "Cidade e UF da vaga são obrigatórias"
		"address"     | new Address(city: "Recife", state: "X") || "Cidade e UF da vaga são obrigatórias"
		"competences" | []                                      || "A vaga deve ter pelo menos uma competência"
		"competences" | ["Java", "Python"]                      || "A vaga deve ter pelo menos uma competência"
	}
}
