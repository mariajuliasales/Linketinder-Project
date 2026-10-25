package com.mariajuliasales.util

import spock.lang.Specification
import spock.lang.Unroll

class CpfValidatorSpec extends Specification {

	@Unroll
	def "cpf '#cpf' should be valid"() {
		expect:
		CpfValidator.isValid(cpf)

		where:
		cpf << [
			"529.982.247-25",
			"52998224725",
			"123.456.789-09"
		]
	}

	@Unroll
	def "cpf '#cpf' should be invalid"() {
		expect:
		!CpfValidator.isValid(cpf)

		where:
		cpf << [
			null,
			"",
			"invalid",
			"529.982.247-26",
			"12345678900",
			"000.000.000-00",
			"111.111.111-11",
			"529.982.247",
			"529.982.247-255"
		]
	}
}
