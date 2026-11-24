package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.RecruiterRequest
import com.mariajuliasales.dto.response.RecruiterResponse
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.model.RecruitmentArea
import spock.lang.Specification

import java.time.LocalDate

import static com.mariajuliasales.fixture.TestData.validRecruiter

class RecruiterMapperSpec extends Specification {

	def "request should be converted to a recruiter"() {
		given:
		RecruiterRequest request = new RecruiterRequest("Rafael Lopes", "rafael@email.com", "123456",
				"Recrutador de tecnologia", "301.958.476-01", LocalDate.of(1988, 9, 3), "ti")

		when:
		Recruiter recruiter = RecruiterMapper.toEntity(request)

		then:
		recruiter.id == null
		recruiter.enterpriseId == null
		recruiter.name == "Rafael Lopes"
		recruiter.email == "rafael@email.com"
		recruiter.password == "123456"
		recruiter.description == "Recrutador de tecnologia"
		recruiter.cpf == "301.958.476-01"
		recruiter.birthDate == LocalDate.of(1988, 9, 3)
		recruiter.area == RecruitmentArea.IT
	}

	def "unknown or missing area should be mapped to null"() {
		given:
		RecruiterRequest request = new RecruiterRequest("Rafael", "rafael@email.com", "123456", null,
				"301.958.476-01", LocalDate.of(1988, 9, 3), area)

		expect:
		RecruiterMapper.toEntity(request).area == null

		where:
		area << [null, "", "Jurídico"]
	}

	def "recruiter should be converted to a response with only id, name, email, area and description"() {
		given:
		Recruiter recruiter = validRecruiter(id: 7)

		when:
		RecruiterResponse response = RecruiterMapper.toResponse(recruiter)

		then:
		response == new RecruiterResponse(7, recruiter.name, recruiter.email, "TI", recruiter.description)
		RecruiterResponse.recordComponents*.name == [
			"id",
			"name",
			"email",
			"area",
			"description"
		]
	}

	def "response should not contain sensitive fields"() {
		given:
		Recruiter recruiter = validRecruiter(id: 7)

		when:
		RecruiterResponse response = RecruiterMapper.toResponse(recruiter)

		then:
		def names = RecruiterResponse.recordComponents*.name
		["cpf", "birthDate", "age", "password", "enterpriseId"].every { !names.contains(it) }
		!response.toString().contains(recruiter.cpf)
		!response.toString().contains(recruiter.password)
	}

	def "recruiter without area should be converted to a response with null area"() {
		expect:
		RecruiterMapper.toResponse(validRecruiter(area: null)).area() == null
	}

	def "null values should be mapped to null"() {
		expect:
		RecruiterMapper.toEntity(null) == null
		RecruiterMapper.toResponse(null) == null
	}
}
