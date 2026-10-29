package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Person
import spock.lang.Specification
import spock.lang.Unroll

import static com.mariajuliasales.fixture.TestData.validEnterprise

class EnterpriseValidatorSpec extends Specification {

	EnterpriseValidator validator = new EnterpriseValidator(new PersonValidator())

	@Unroll
	def "enterprise with valid cnpj '#cnpj' should pass"() {
		when:
		validator.validateForCreate(validEnterprise(cnpj: cnpj))

		then:
		noExceptionThrown()

		where:
		cnpj << [
			"11.222.333/0001-81",
			"00.000.000/E08G-12"
		]
	}

	def "enterprise validation should include the common person rules"() {
		when:
		validator.validateForUpdate(validEnterprise(name: ""))

		then:
		ValidationException exception = thrown()
		exception.message == "Nome é obrigatório"
	}

	@Unroll
	def "enterprise with invalid cnpj '#cnpj' should fail"() {
		when:
		validator.validateForCreate(validEnterprise(cnpj: cnpj))

		then:
		ValidationException exception = thrown()
		exception.message == "CNPJ inválido"

		where:
		cnpj << [
			null,
			"",
			"cnpj_invalido",
			"00.000.000/E08G-99",
			"AA.AAA.AAA/AAAA-AA"
		]
	}

	def "common person rules should be delegated to the injected person validator"() {
		given:
		Validator<Person> personValidator = Mock()
		Enterprise enterprise = validEnterprise()

		when:
		new EnterpriseValidator(personValidator).validateForCreate(enterprise)

		then:
		1 * personValidator.validateForCreate(enterprise)
	}
}
