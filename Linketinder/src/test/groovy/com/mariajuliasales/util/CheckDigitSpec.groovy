package com.mariajuliasales.util

import spock.lang.Specification
import spock.lang.Unroll

class CheckDigitSpec extends Specification {

	static final List<Integer> CPF_FIRST_WEIGHTS = (10..2).toList()
	static final List<Integer> CPF_SECOND_WEIGHTS = (11..2).toList()

	@Unroll
	def "document '#document' should match its check digits = #expected"() {
		expect:
		CheckDigit.matches(document, CPF_FIRST_WEIGHTS, CPF_SECOND_WEIGHTS) == expected

		where:
		document      || expected
		"52998224725" || true
		"52998224726" || false
		"52998224735" || false
	}

	@Unroll
	def "document '#document' should be a repeated sequence = #expected"() {
		expect:
		CheckDigit.isRepeatedSequence(document) == expected

		where:
		document         || expected
		"00000000000"    || true
		"AAAAAAAAAAAAAA" || true
		"52998224725"    || false
	}
}
