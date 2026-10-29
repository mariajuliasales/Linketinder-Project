package com.mariajuliasales.util

import spock.lang.Specification
import spock.lang.Unroll

class CnpjValidatorSpec extends Specification {

	@Unroll
	def "cnpj '#cnpj' should be valid"() {
		expect:
		CnpjValidator.isValid(cnpj)

		where:
		cnpj << [
			"11.222.333/0001-81",
			"11222333000181",
			"12.ABC.345/01DE-35",
			"12abc34501de35",
			"00.000.000/E08G-12"
		]
	}

	@Unroll
	def "cnpj '#cnpj' should be invalid"() {
		expect:
		!CnpjValidator.isValid(cnpj)

		where:
		cnpj << [
			null,
			"",
			"   ",
			"cnpj_invalido",
			"11.222.333/0001-82",
			"00.000.000/0000-00",
			"AA.AAA.AAA/AAAA-AA",
			"12.ABC.345/01D",
			"11 222 333 0001 81"
		]
	}
}
