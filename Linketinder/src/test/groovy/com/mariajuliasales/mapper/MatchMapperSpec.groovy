package com.mariajuliasales.mapper

import com.mariajuliasales.dto.response.CandidateMatchResponse
import com.mariajuliasales.dto.response.MatchedVacancyResponse
import com.mariajuliasales.dto.response.VacancyMatchResponse
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validCandidate
import static com.mariajuliasales.fixture.TestData.validVacancy

class MatchMapperSpec extends Specification {

	static final List<String> FORBIDDEN = [
		"cpf",
		"birthDate",
		"age",
		"address",
		"password",
		"cnpj",
		"enterpriseId"
	]

	def "candidate without match should have only anonymous data"() {
		given:
		Candidate candidate = validCandidate(id: 3)

		when:
		CandidateMatchResponse response = MatchMapper.toCandidateMatchResponse(candidate, matched)

		then:
		response.id() == 3
		response.training() == candidate.training
		response.description() == candidate.description
		response.competences() == ["Java"]
		response.name() == null
		response.email() == null
		response.matchedVacancies() == []

		where:
		matched << [null, []]
	}

	def "candidate with match should reveal name, email and only the matched vacancies"() {
		given:
		Candidate candidate = validCandidate(id: 3)
		Vacancy v1 = validVacancy(id: 10, title: "Dev Groovy", status: VacancyStatus.PAUSED)

		when:
		CandidateMatchResponse response = MatchMapper.toCandidateMatchResponse(candidate, [v1])

		then:
		response.name() == candidate.name
		response.email() == candidate.email
		response.matchedVacancies() == [
			new MatchedVacancyResponse(10, "Dev Groovy", "PAUSED")
		]
	}

	def "candidate response should never contain sensitive data, with or without match"() {
		given:
		Candidate candidate = validCandidate(id: 3)

		when:
		CandidateMatchResponse response = MatchMapper.toCandidateMatchResponse(candidate, matched)

		then:
		def names = CandidateMatchResponse.recordComponents*.name
		FORBIDDEN.every { !names.contains(it) }
		CandidateMatchResponse.recordComponents*.name == [
			"id",
			"training",
			"description",
			"competences",
			"name",
			"email",
			"matchedVacancies"
		]
		!response.toString().contains(candidate.cpf)
		!response.toString().contains(candidate.password)
		!response.toString().contains(candidate.address.street)
		!response.toString().contains(candidate.address.cep)
		!response.toString().contains(candidate.birthDate.toString())

		where:
		matched << [[], [validVacancy(id: 10)]]
	}

	def "matched vacancy response should contain only id, title and status"() {
		expect:
		MatchedVacancyResponse.recordComponents*.name == ["id", "title", "status"]
		MatchMapper.toMatchedVacancyResponse(validVacancy(id: 10)) == new MatchedVacancyResponse(10, "Desenvolvedor Groovy", "OPEN")
	}

	def "vacancy without match should not reveal the enterprise"() {
		given:
		Vacancy vacancy = validVacancy(id: 20, status: VacancyStatus.PAUSED)

		when:
		VacancyMatchResponse response = MatchMapper.toVacancyMatchResponse(vacancy, false)

		then:
		response == new VacancyMatchResponse(20, vacancy.title, vacancy.description, "São Paulo", "SP", "PAUSED",
				["Groovy"], null, null)
		!response.toString().contains(vacancy.enterprise.name)
		!response.toString().contains(vacancy.enterprise.email)
		!response.toString().contains(vacancy.enterprise.cnpj)
	}

	def "vacancy with match should reveal only enterprise name and email"() {
		given:
		Vacancy vacancy = validVacancy(id: 20)

		when:
		VacancyMatchResponse response = MatchMapper.toVacancyMatchResponse(vacancy, true)

		then:
		response.enterpriseName() == vacancy.enterprise.name
		response.enterpriseEmail() == vacancy.enterprise.email
		response.status() == "OPEN"
		!response.toString().contains(vacancy.enterprise.cnpj)
		!response.toString().contains(vacancy.enterprise.password)
		!response.toString().contains(vacancy.address.street)
	}

	def "vacancy response should never contain sensitive components"() {
		expect:
		def names = VacancyMatchResponse.recordComponents*.name
		FORBIDDEN.every { !names.contains(it) }
		names == [
			"id",
			"title",
			"description",
			"city",
			"state",
			"status",
			"competences",
			"enterpriseName",
			"enterpriseEmail"
		]
	}

	def "null values should be mapped to null or empty"() {
		expect:
		MatchMapper.toCandidateMatchResponse(null, [validVacancy()]) == null
		MatchMapper.toVacancyMatchResponse(null, true) == null
		MatchMapper.toMatchedVacancyResponse(null) == null
		MatchMapper.toMatchedVacancyResponses(null) == []
	}
}
