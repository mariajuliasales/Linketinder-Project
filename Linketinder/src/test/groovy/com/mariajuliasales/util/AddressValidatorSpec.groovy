package com.mariajuliasales.util

import com.mariajuliasales.model.Address
import spock.lang.Specification
import spock.lang.Unroll

class AddressValidatorSpec extends Specification {

	@Unroll
	def "cep '#cep' should be valid = #expected"() {
		expect:
		AddressValidator.isValidCep(cep) == expected

		where:
		cep         || expected
		"01310-000" || true
		"01310000"  || true
		"0131000"   || false
		"abcde-fgh" || false
		null        || false
	}

	@Unroll
	def "state '#state' should be valid = #expected"() {
		expect:
		AddressValidator.isValidState(state) == expected

		where:
		state || expected
		"SP"  || true
		"mg"  || true
		"S"   || false
		"SPX" || false
		"S1"  || false
		null  || false
	}

	@Unroll
	def "location with city '#city' and state '#state' should be valid = #expected"() {
		expect:
		AddressValidator.isValidLocation(new Address(city: city, state: state)) == expected

		where:
		city        | state || expected
		"São Paulo" | "SP"  || true
		""          | "SP"  || false
		"   "       | "SP"  || false
		null        | "SP"  || false
		"São Paulo" | "SPX" || false
	}

	def "null address should not be a valid location"() {
		expect:
		!AddressValidator.isValidLocation(null)
	}
}
