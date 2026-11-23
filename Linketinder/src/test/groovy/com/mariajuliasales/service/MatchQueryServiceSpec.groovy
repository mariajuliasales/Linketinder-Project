package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.CandidateMatches
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository
import spock.lang.Specification

import java.sql.Connection

import static com.mariajuliasales.fixture.TestData.validCandidate
import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validRecruiter

class MatchQueryServiceSpec extends Specification {

	static final int E1 = 3
	static final int E2 = 4
	static final int R1 = 7
	static final int R2 = 8
	static final int C5 = 5
	static final int C6 = 6
	static final int C9 = 9

	Connection connection = Stub()
	ConnectionManager connectionManager = Stub() {
		withConnection(_) >> { Closure work -> work(connection) }
	}
	CandidateRepository candidateRepository = Mock()
	EnterpriseRepository enterpriseRepository = Stub() {
		findById(E1) >> validEnterprise(id: E1)
		findById(E2) >> validEnterprise(id: E2)
		findById(_) >> null
	}
	RecruiterRepository recruiterRepository = Stub() {
		findById(R1) >> validRecruiter(id: R1, enterpriseId: E1)
		findById(R2) >> validRecruiter(id: R2, enterpriseId: E2)
		findById(_) >> null
	}
	MatchRepository matchRepository = Mock()
	MatchQueryService service = new MatchQueryService(connectionManager, candidateRepository, enterpriseRepository,
	recruiterRepository, matchRepository)

	private static Vacancy vacancy(int id) {
		new Vacancy(id: id, title: "Vaga ${id}", status: VacancyStatus.OPEN)
	}

	def "enterprise list should order candidates and their vacancies by ascending id"() {
		given:
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C9): [vacancy(12), vacancy(10)], (C5): [vacancy(11)]]
		candidateRepository.findAllByIds(_) >> [validCandidate(id: C9), validCandidate(id: C5)]

		when:
		List<CandidateMatches> result = service.candidatesWithMatchesForEnterprise(E1)

		then:
		result*.candidate()*.id == [C5, C9]
		result[0].vacancies()*.id == [11]
		result[1].vacancies()*.id == [10, 12]
	}

	def "candidate without match, even with a one-sided like, should not appear in the list"() {
		given: 'C6 curtiu uma vaga de E1 (ou foi curtido por E1), mas não tem vacancy_match: a consulta não o traz'
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [vacancy(10)]]

		when:
		List<CandidateMatches> result = service.candidatesWithMatchesForEnterprise(E1)

		then: 'só os ids com match são buscados'
		1 * candidateRepository.findAllByIds({ it as Set == [C5] as Set }) >> [validCandidate(id: C5)]
		0 * candidateRepository._
		result*.candidate()*.id == [C5]
	}

	def "candidates returned by the repository without a match should be left out"() {
		given:
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [vacancy(10)]]
		candidateRepository.findAllByIds(_) >> [validCandidate(id: C5), validCandidate(id: C6)]

		expect:
		service.candidatesWithMatchesForEnterprise(E1)*.candidate()*.id == [C5]
	}

	def "enterprise without matches should get an empty list without searching candidates"() {
		given:
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [:]

		when:
		List<CandidateMatches> result = service.candidatesWithMatchesForEnterprise(E1)

		then:
		result == []
		0 * candidateRepository.findAllByIds(_)
	}

	def "candidates and matched vacancies should be loaded with one query each"() {
		when:
		service.candidatesWithMatchesForEnterprise(E1)

		then:
		1 * matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [vacancy(10)], (C9): [vacancy(11)]]
		1 * candidateRepository.findAllByIds({ it as Set == [C5, C9] as Set }) >> [validCandidate(id: C5), validCandidate(id: C9)]
		0 * matchRepository._
		0 * candidateRepository._
	}

	def "missing enterprise should throw not found without querying matches"() {
		when:
		service.candidatesWithMatchesForEnterprise(99)

		then:
		EntityNotFoundException error = thrown()
		error.message.contains('Empresa')
		0 * matchRepository._
		0 * candidateRepository._
	}

	def "missing recruiter should throw not found without querying matches"() {
		when:
		service.candidatesWithMatchesForRecruiter(99)

		then:
		EntityNotFoundException error = thrown()
		error.message.contains('Recrutador')
		0 * matchRepository._
		0 * candidateRepository._
	}

	def "recruiter list should use the recruiter's own enterprise"() {
		when:
		List<CandidateMatches> result = service.candidatesWithMatchesForRecruiter(R1)

		then:
		1 * matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [vacancy(10)]]
		0 * matchRepository.matchedVacanciesByEnterprise(connection, E2)
		1 * candidateRepository.findAllByIds(_) >> [validCandidate(id: C5)]
		result*.candidate()*.id == [C5]
	}

	def "match with another enterprise should not reveal the candidate to this recruiter"() {
		given: 'C5 tem match só com E1'
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [vacancy(10)]]
		matchRepository.matchedVacanciesByEnterprise(connection, E2) >> [:]

		expect:
		service.candidatesWithMatchesForRecruiter(R2) == []
	}

	def "candidate matches should keep an immutable copy of the vacancies"() {
		given:
		List<Vacancy> vacancies = [vacancy(10)]
		CandidateMatches matches = new CandidateMatches(validCandidate(id: C5), vacancies)

		when:
		vacancies << vacancy(11)

		then:
		matches.vacancies()*.id == [10]

		when:
		matches.vacancies().add(vacancy(12))

		then:
		thrown(UnsupportedOperationException)
	}
}
