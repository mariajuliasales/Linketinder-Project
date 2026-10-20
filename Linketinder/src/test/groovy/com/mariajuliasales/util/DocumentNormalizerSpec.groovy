package com.mariajuliasales.util

import spock.lang.Specification
import spock.lang.Unroll

class DocumentNormalizerSpec extends Specification {

	@Unroll
	def "only digits of '#value' should be '#expected'"() {
		expect:
		DocumentNormalizer.onlyDigits(value) == expected

		where:
		value            || expected
		"529.982.247-25" || "52998224725"
		"01310-000"      || "01310000"
		"52998224725"    || "52998224725"
		null             || null
	}

	@Unroll
	def "normalized cnpj of '#cnpj' should be '#expected'"() {
		expect:
		DocumentNormalizer.normalizeCnpj(cnpj) == expected

		where:
		cnpj                 || expected
		"11.222.333/0001-81" || "11222333000181"
		"12.abc.345/01de-35" || "12ABC34501DE35"
		"11 222 333 0001 81" || "11222333000181"
		null                 || null
	}
}
