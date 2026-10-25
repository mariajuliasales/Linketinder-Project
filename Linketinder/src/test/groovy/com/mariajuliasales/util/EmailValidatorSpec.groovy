package com.mariajuliasales.util

import spock.lang.Specification
import spock.lang.Unroll

class EmailValidatorSpec extends Specification {

	@Unroll
	def "email '#email' should be valid"() {
		expect:
		EmailValidator.isValid(email)

		where:
		email << [
			"joao@email.com",
			"maria.sales+vagas@empresa.com.br",
			"rh@inovacaodigital.io"
		]
	}

	@Unroll
	def "email '#email' should be invalid"() {
		expect:
		!EmailValidator.isValid(email)

		where:
		email << [
			null,
			"",
			"invalid",
			"joaoemail.com",
			"joao@.com",
			"joao@email",
			"joao@email.",
			"joao @email.com"
		]
	}
}
