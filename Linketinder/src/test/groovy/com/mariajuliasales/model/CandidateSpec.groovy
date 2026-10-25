package com.mariajuliasales.model

import spock.lang.Specification

import java.time.LocalDate

class CandidateSpec extends Specification {

	def "age should be calculated from the birth date"() {
		given:
		Candidate candidate = new Candidate(birthDate: LocalDate.now().minusYears(25))

		expect:
		candidate.age == 25
	}

	def "age should not count the birthday that has not happened yet this year"() {
		given:
		Candidate candidate = new Candidate(birthDate: LocalDate.now().minusYears(25).plusDays(1))

		expect:
		candidate.age == 24
	}

	def "age should be zero without a birth date"() {
		expect:
		new Candidate().age == 0
	}
}
