package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.dto.response.CandidateResponse
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence
import spock.lang.Specification

import java.time.LocalDate

import static com.mariajuliasales.fixture.TestData.validCandidate

class CandidateMapperSpec extends Specification {

	def "request should be converted to a candidate"() {
		given:
		AddressRequest address = new AddressRequest("01310-000", null, null, null, null, "São Paulo", "SP", "Brasil")
		CandidateRequest request = new CandidateRequest("João Silva", "joao@email.com", "123456", address,
				"Desenvolvedor", ["Java", "Groovy"], "529.982.247-25", LocalDate.of(1995, 5, 20), "Computação")

		when:
		Candidate candidate = CandidateMapper.toEntity(request)

		then:
		candidate.id == null
		candidate.name == "João Silva"
		candidate.email == "joao@email.com"
		candidate.password == "123456"
		candidate.address.city == "São Paulo"
		candidate.description == "Desenvolvedor"
		candidate.competences == [
			new Competence("Java"),
			new Competence("Groovy")
		]
		candidate.cpf == "529.982.247-25"
		candidate.birthDate == LocalDate.of(1995, 5, 20)
		candidate.training == "Computação"
	}

	def "candidate should be converted to a full response without the password"() {
		given:
		Candidate candidate = validCandidate(id: 7)

		when:
		CandidateResponse response = CandidateMapper.toResponse(candidate)

		then:
		response.id() == 7
		response.name() == candidate.name
		response.email() == candidate.email
		response.cpf() == candidate.cpf
		response.age() == candidate.age
		response.address().city() == "São Paulo"
		response.competences() == ["Java"]
		!CandidateResponse.recordComponents*.name.contains("password")
	}

	def "candidate should be converted to an anonymous response without personal data"() {
		given:
		Candidate candidate = validCandidate(id: 7)

		when:
		CandidateAnonymousResponse response = CandidateMapper.toAnonymousResponse(candidate)

		then:
		response == new CandidateAnonymousResponse(7, candidate.training, candidate.description, ["Java"])
		CandidateAnonymousResponse.recordComponents*.name == [
			"id",
			"training",
			"description",
			"competences"
		]
	}

	def "null values should be mapped to null"() {
		expect:
		CandidateMapper.toEntity(null) == null
		CandidateMapper.toResponse(null) == null
		CandidateMapper.toAnonymousResponse(null) == null
	}
}
