package com.mariajuliasales.util

import spock.lang.Specification
import spock.lang.Unroll

class PasswordValidatorSpec extends Specification {

	@Unroll
	def "password '#password' should be valid = #expected"() {
		expect:
		PasswordValidator.isValid(password) == expected

		where:
		password   || expected
		"123456"   || true
		"senha123" || true
		"12345"    || false
		""         || false
		null       || false
	}
}
