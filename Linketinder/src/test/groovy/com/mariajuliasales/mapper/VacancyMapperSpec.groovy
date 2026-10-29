package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.VacancyRequest
import com.mariajuliasales.dto.response.VacancyAnonymousResponse
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import spock.lang.Specification
import spock.lang.Unroll

import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validVacancy

class VacancyMapperSpec extends Specification {

	def "request should be converted to a vacancy of the given enterprise"() {
		given:
		Enterprise enterprise = validEnterprise()
		VacancyRequest request = new VacancyRequest("Dev Groovy", "APIs", "Recife", "PE", "PAUSED", ["Groovy"])

		when:
		Vacancy vacancy = VacancyMapper.toEntity(request, enterprise)

		then:
		vacancy.id == null
		vacancy.title == "Dev Groovy"
		vacancy.description == "APIs"
		vacancy.address.city == "Recife"
		vacancy.address.state == "PE"
		vacancy.status == VacancyStatus.PAUSED
		vacancy.competences == [new Competence("Groovy")]
		vacancy.enterprise.is(enterprise)
	}

	@Unroll
	def "status '#status' should be converted to #expected"() {
		given:
		VacancyRequest request = new VacancyRequest("Dev", "APIs", "Recife", "PE", status, ["Groovy"])

		expect:
		VacancyMapper.toEntity(request, validEnterprise()).status == expected

		where:
		status   || expected
		null     || VacancyStatus.OPEN
		""       || VacancyStatus.OPEN
		"closed" || VacancyStatus.CLOSED
		" OPEN " || VacancyStatus.OPEN
	}

	def "unknown status should fail"() {
		given:
		VacancyRequest request = new VacancyRequest("Dev", "APIs", "Recife", "PE", "ARCHIVED", ["Groovy"])

		when:
		VacancyMapper.toEntity(request, validEnterprise())

		then:
		ValidationException exception = thrown()
		exception.message == "Status inválido. Use: OPEN, CLOSED, PAUSED"
	}

	def "vacancy should be converted to an anonymous response without the enterprise"() {
		given:
		Vacancy vacancy = validVacancy(id: 5)

		when:
		VacancyAnonymousResponse response = VacancyMapper.toAnonymousResponse(vacancy)

		then:
		response == new VacancyAnonymousResponse(5, vacancy.title, vacancy.description, "São Paulo", "SP", "OPEN", ["Groovy"])
		!VacancyAnonymousResponse.recordComponents*.name.any { it.startsWith("enterprise") }
	}

	def "vacancy list should be converted to a list of anonymous responses"() {
		expect:
		VacancyMapper.toAnonymousResponses([
			validVacancy(id: 1),
			validVacancy(id: 2)
		])*.id() == [1, 2]
		VacancyMapper.toAnonymousResponses(null) == []
	}

	def "null values should be mapped to null"() {
		expect:
		VacancyMapper.toEntity(null, validEnterprise()) == null
		VacancyMapper.toAnonymousResponse(null) == null
	}
}
