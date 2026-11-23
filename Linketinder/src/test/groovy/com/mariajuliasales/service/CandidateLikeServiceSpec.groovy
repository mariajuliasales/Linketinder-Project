package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.VacancyRepository
import spock.lang.Specification

import java.sql.Connection

import static com.mariajuliasales.fixture.TestData.validCandidate
import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validVacancy

class CandidateLikeServiceSpec extends Specification {

	static final int CANDIDATE = 5
	static final int VACANCY = 10
	static final int ENTERPRISE = 3

	Connection connection = Stub()
	ConnectionManager connectionManager = Stub() {
		withTransaction(_) >> { Closure work -> work(connection) }
	}
	CandidateRepository candidateRepository = Stub() {
		findById(CANDIDATE) >> validCandidate(id: CANDIDATE)
		findById(_) >> null
	}
	VacancyRepository vacancyRepository = Mock()
	LikeRepository likeRepository = Mock()
	MatchRepository matchRepository = Mock()
	CandidateLikeService service = new CandidateLikeService(connectionManager, candidateRepository, vacancyRepository,
	likeRepository, matchRepository)

	def setup() {
		likeRepository.lockEnterprise(_, ENTERPRISE) >> true
	}

	def "like should fail with 404 without revealing the enterprise and save nothing when the enterprise no longer exists at lock time"() {
		given:
		vacancyRepository.findById(VACANCY) >> openVacancy()

		when:
		service.like(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> false
		EntityNotFoundException exception = thrown()
		exception.message == "Vaga com ID ${VACANCY} não existe"
		0 * likeRepository._
		0 * matchRepository._
	}

	def "like should lock the enterprise, then the vacancy, and save the like without match when no recruiter of the enterprise likes the candidate (B6)"() {
		given:
		vacancyRepository.findById(VACANCY) >> openVacancy()

		when:
		service.like(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> true

		then:
		1 * likeRepository.lockVacancyStatus(connection, VACANCY) >> VacancyStatus.OPEN

		then:
		1 * likeRepository.insertCandidateLike(connection, CANDIDATE, VACANCY) >> true
		1 * likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> false
		0 * matchRepository.insertMatch(*_)
	}

	def "like should create the match in the same transaction when the enterprise already likes the candidate (B11)"() {
		given:
		vacancyRepository.findById(VACANCY) >> openVacancy()

		when:
		service.like(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> true

		then:
		1 * likeRepository.lockVacancyStatus(connection, VACANCY) >> VacancyStatus.OPEN

		then:
		1 * likeRepository.insertCandidateLike(connection, CANDIDATE, VACANCY) >> true

		then:
		1 * likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> true

		then:
		1 * matchRepository.insertMatch(connection, CANDIDATE, VACANCY) >> true
	}

	def "like should fail with 400 and save nothing when the vacancy is #status"() {
		given:
		vacancyRepository.findById(VACANCY) >> openVacancy(status: status)

		when:
		service.like(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.lockVacancyStatus(connection, VACANCY) >> status
		ValidationException exception = thrown()
		exception.message == "Só é possível curtir vagas abertas"
		0 * likeRepository.insertCandidateLike(*_)
		0 * matchRepository.insertMatch(*_)

		where:
		status << [VacancyStatus.PAUSED, VacancyStatus.CLOSED]
	}

	def "like should fail with 400 and save nothing when the vacancy is closed between the first read and the lock"() {
		given:
		vacancyRepository.findById(VACANCY) >> openVacancy(status: VacancyStatus.OPEN)

		when:
		service.like(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> true

		then:
		1 * likeRepository.lockVacancyStatus(connection, VACANCY) >> VacancyStatus.CLOSED
		ValidationException exception = thrown()
		exception.message == "Só é possível curtir vagas abertas"
		0 * likeRepository.insertCandidateLike(*_)
		0 * likeRepository.enterpriseLikeExists(*_)
		0 * matchRepository._
	}

	def "like should fail with 404 and save nothing when the vacancy does not exist"() {
		given:
		vacancyRepository.findById(99) >> null

		when:
		service.like(CANDIDATE, 99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Vaga com ID 99 não existe"
		0 * likeRepository._
		0 * matchRepository._
	}

	def "like should fail with 404 and save nothing when the vacancy is deleted between the first read and the lock"() {
		given:
		vacancyRepository.findById(VACANCY) >> openVacancy()

		when:
		service.like(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> true
		1 * likeRepository.lockVacancyStatus(connection, VACANCY) >> null
		EntityNotFoundException exception = thrown()
		exception.message == "Vaga com ID ${VACANCY} não existe"
		0 * likeRepository.insertCandidateLike(*_)
		0 * matchRepository._
	}

	def "like should fail with 404 and save nothing when the candidate does not exist"() {
		when:
		service.like(99, VACANCY)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Candidato com ID 99 não existe"
		0 * vacancyRepository._
		0 * likeRepository._
		0 * matchRepository._
	}

	def "like should not fail when the candidate already likes the vacancy"() {
		given:
		vacancyRepository.findById(VACANCY) >> openVacancy()
		likeRepository.lockVacancyStatus(connection, VACANCY) >> VacancyStatus.OPEN

		when:
		service.like(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.insertCandidateLike(connection, CANDIDATE, VACANCY) >> false
		1 * likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> false
		noExceptionThrown()
	}

	def "unlike should remove only the candidate like, keeping the enterprise like and other matches (B12)"() {
		when:
		service.unlike(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.deleteCandidateLike(connection, CANDIDATE, VACANCY) >> true
		0 * likeRepository.deleteEnterpriseLike(*_)
		0 * matchRepository._
	}

	def "unlike should not fail when the candidate did not like the vacancy"() {
		when:
		service.unlike(CANDIDATE, VACANCY)

		then:
		1 * likeRepository.deleteCandidateLike(connection, CANDIDATE, VACANCY) >> false
		noExceptionThrown()
	}

	def "unlike should work whatever the vacancy status, without loading the vacancy"() {
		when:
		service.unlike(CANDIDATE, VACANCY)

		then:
		0 * vacancyRepository._
		1 * likeRepository.deleteCandidateLike(connection, CANDIDATE, VACANCY)
	}

	private static openVacancy(Map changes = [:]) {
		validVacancy([id: VACANCY, status: VacancyStatus.OPEN, enterprise: validEnterprise(id: ENTERPRISE)] + changes)
	}
}
