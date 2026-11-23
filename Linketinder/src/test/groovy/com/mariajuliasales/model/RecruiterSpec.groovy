package com.mariajuliasales.model

import com.mariajuliasales.fixture.TestData
import spock.lang.Specification

import java.time.LocalDate

class RecruiterSpec extends Specification {

	def "age should be calculated from the birth date"() {
		given:
		Recruiter recruiter = new Recruiter(birthDate: LocalDate.now().minusYears(30))

		expect:
		recruiter.age == 30
	}

	def "age should not count the birthday that has not happened yet this year"() {
		given:
		Recruiter recruiter = new Recruiter(birthDate: LocalDate.now().minusYears(18).plusDays(1))

		expect:
		recruiter.age == 17
	}

	def "age should be zero without a birth date"() {
		expect:
		new Recruiter().age == 0
	}

	def "recruiter should never have an address"() {
		given:
		Recruiter recruiter = TestData.validRecruiter(address: TestData.validAddress())

		expect:
		recruiter.address == null
	}

	def "recruiter should never have competences"() {
		given:
		Recruiter recruiter = TestData.validRecruiter(competences: [new Competence("Java")])

		expect:
		recruiter.competences.isEmpty()
	}

	def "recruiter should be a person"() {
		expect:
		TestData.validRecruiter() instanceof Person
	}

	def "toString should not expose cpf, password or email"() {
		given:
		Recruiter recruiter = TestData.validRecruiter()

		when:
		String text = recruiter.toString()

		then:
		!text.contains(recruiter.cpf)
		!text.contains(recruiter.password)
		!text.contains(recruiter.email)
	}

	def "validRecruiter should build a recruiter linked to an enterprise and an area"() {
		given:
		Recruiter recruiter = TestData.validRecruiter(area: RecruitmentArea.HR)

		expect:
		recruiter.enterpriseId == 1
		recruiter.area == RecruitmentArea.HR
		recruiter.age >= 18
	}
}
