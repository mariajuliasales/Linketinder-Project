package com.mariajuliasales.service

import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Candidate
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validCandidate

class CandidateServiceSpec extends Specification {

	CandidateDAO candidateDAO = Mock(CandidateDAO)
	CandidateService candidateService = new CandidateService(candidateDAO)

	def "create should save a valid candidate"() {
		given:
		Candidate candidate = validCandidate()

		when:
		Candidate result = candidateService.create(candidate)

		then:
		1 * candidateDAO.create(candidate) >> candidate
		result == candidate
	}

	def "create should not save an invalid candidate"() {
		when:
		candidateService.create(validCandidate(cpf: "invalid"))

		then:
		thrown(ValidationException)
		0 * candidateDAO.create(_)
	}

	def "create should require a password"() {
		when:
		candidateService.create(validCandidate(password: null))

		then:
		thrown(ValidationException)
		0 * candidateDAO.create(_)
	}

	def "find all should return the candidates from the DAO"() {
		given:
		List<Candidate> candidates = [
			validCandidate(id: 1),
			validCandidate(id: 2)
		]
		candidateDAO.findAll() >> candidates

		expect:
		candidateService.findAll() == candidates
	}

	def "find by id should return the candidate from the DAO"() {
		given:
		Candidate candidate = validCandidate(id: 10)
		candidateDAO.findById(10) >> candidate

		expect:
		candidateService.findById(10) == candidate
	}

	def "find by id should fail when the candidate does not exist"() {
		given:
		candidateDAO.findById(99) >> null

		when:
		candidateService.findById(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Candidato com ID 99 não existe"
	}

	def "update should accept a candidate without a new password"() {
		given:
		Candidate candidate = validCandidate(password: "")

		when:
		Candidate result = candidateService.update(candidate)

		then:
		1 * candidateDAO.update(candidate) >> candidate
		result == candidate
	}

	def "update should not save an invalid candidate"() {
		when:
		candidateService.update(validCandidate(training: ""))

		then:
		thrown(ValidationException)
		0 * candidateDAO.update(_)
	}

	def "update should fail when the candidate does not exist"() {
		given:
		candidateDAO.update(_) >> null

		when:
		candidateService.update(validCandidate(id: 99))

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Candidato com ID 99 não existe"
	}

	def "delete should remove the candidate through the DAO"() {
		when:
		candidateService.delete(1)

		then:
		1 * candidateDAO.delete(1) >> true
	}

	def "delete should fail when the candidate does not exist"() {
		given:
		candidateDAO.delete(99) >> false

		when:
		candidateService.delete(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Candidato com ID 99 não existe"
	}
}
