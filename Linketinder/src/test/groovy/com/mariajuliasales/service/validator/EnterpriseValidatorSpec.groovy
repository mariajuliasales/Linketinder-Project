package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import spock.lang.Specification
import spock.lang.Unroll

import static com.mariajuliasales.fixture.TestData.validEnterprise

class EnterpriseValidatorSpec extends Specification {

	@Unroll
	def "enterprise with valid cnpj '#cnpj' should pass"() {
		when:
		EnterpriseValidator.validateForCreate(validEnterprise(cnpj: cnpj))

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
		EnterpriseValidator.validateForUpdate(validEnterprise(name: ""))

		then:
		ValidationException exception = thrown()
		exception.message == "Nome é obrigatório"
	}

	@Unroll
	def "enterprise with invalid cnpj '#cnpj' should fail"() {
		when:
		EnterpriseValidator.validateForCreate(validEnterprise(cnpj: cnpj))

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
}
