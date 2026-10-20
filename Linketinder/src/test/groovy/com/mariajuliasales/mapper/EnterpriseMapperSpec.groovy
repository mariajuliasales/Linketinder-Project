package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.request.EnterpriseRequest
import com.mariajuliasales.dto.response.EnterpriseAnonymousResponse
import com.mariajuliasales.dto.response.EnterpriseResponse
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validVacancy

class EnterpriseMapperSpec extends Specification {

	def "request should be converted to an enterprise"() {
		given:
		AddressRequest address = new AddressRequest("01310-000", null, null, null, null, "São Paulo", "SP", "Brasil")
		EnterpriseRequest request = new EnterpriseRequest("TechSolutions", "rh@tech.com", "123456", address,
				"Software", ["Java"], "11.222.333/0001-81")

		when:
		Enterprise enterprise = EnterpriseMapper.toEntity(request)

		then:
		enterprise.id == null
		enterprise.name == "TechSolutions"
		enterprise.email == "rh@tech.com"
		enterprise.password == "123456"
		enterprise.address.state == "SP"
		enterprise.description == "Software"
		enterprise.competences == [new Competence("Java")]
		enterprise.cnpj == "11.222.333/0001-81"
		enterprise.vacancies.isEmpty()
	}

	def "enterprise should be converted to a full response with its vacancies"() {
		given:
		Enterprise enterprise = validEnterprise(id: 3)
		enterprise.addVacancy(validVacancy(id: 10))

		when:
		EnterpriseResponse response = EnterpriseMapper.toResponse(enterprise)

		then:
		response.id() == 3
		response.name() == enterprise.name
		response.cnpj() == enterprise.cnpj
		response.competences() == ["Java"]
		response.vacancies()*.id() == [10]
		!EnterpriseResponse.recordComponents*.name.contains("password")
	}

	def "enterprise should be converted to an anonymous response with the number of vacancies"() {
		given:
		Enterprise enterprise = validEnterprise(id: 3)
		enterprise.addVacancy(validVacancy(id: 10))
		enterprise.addVacancy(validVacancy(id: 11))

		when:
		EnterpriseAnonymousResponse response = EnterpriseMapper.toAnonymousResponse(enterprise)

		then:
		response == new EnterpriseAnonymousResponse(3, "SP", "Brasil", enterprise.description, ["Java"], 2)
	}

	def "null values should be mapped to null"() {
		expect:
		EnterpriseMapper.toEntity(null) == null
		EnterpriseMapper.toResponse(null) == null
		EnterpriseMapper.toAnonymousResponse(null) == null
	}
}
