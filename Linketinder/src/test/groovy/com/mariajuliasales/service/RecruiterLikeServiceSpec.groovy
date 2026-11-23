package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionFactory
import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.dao.DatabaseConnection
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.RecruitmentArea
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository
import spock.lang.Specification

import java.sql.Connection

import static com.mariajuliasales.fixture.TestData.validCandidate
import static com.mariajuliasales.fixture.TestData.validRecruiter

class RecruiterLikeServiceSpec extends Specification {

	static final int R1 = 7
	static final int R2 = 8
	static final int CANDIDATE = 5
	static final int ENTERPRISE = 3
	static final int V1 = 10
	static final int V2 = 11

	Connection connection = Stub()
	ConnectionManager connectionManager = Stub() {
		withTransaction(_) >> { Closure work -> work(connection) }
	}
	RecruiterRepository recruiterRepository = Stub() {
		findById(R1) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)
		findById(R2) >> validRecruiter(id: R2, enterpriseId: ENTERPRISE, cpf: '714.602.380-01')
		findById(_) >> null
	}
	CandidateRepository candidateRepository = Stub() {
		findById(CANDIDATE) >> validCandidate(id: CANDIDATE)
		findById(_) >> null
	}
	LikeRepository likeRepository = Mock()
	MatchRepository matchRepository = Mock()
	RecruiterLikeService service = new RecruiterLikeService(connectionManager, recruiterRepository,
	candidateRepository, likeRepository, matchRepository)

	def setup() {
		likeRepository.lockEnterprise(_, ENTERPRISE) >> true
	}

	def "first like of the enterprise should check the enterprise like before saving the recruiter like and create the matches"() {
		when:
		service.like(R1, CANDIDATE)

		then:
		1 * likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> false
		0 * likeRepository.insertRecruiterLike(*_)

		then:
		1 * likeRepository.insertRecruiterLike(connection, R1, CANDIDATE) >> true
		1 * likeRepository.insertEnterpriseLike(connection, ENTERPRISE, CANDIDATE) >> true
		1 * matchRepository.likedVacanciesOfEnterprise(connection, CANDIDATE, ENTERPRISE) >> [(V1): VacancyStatus.OPEN]
		1 * matchRepository.insertMatch(connection, CANDIDATE, V1) >> true
	}

	def "like should fail with 404 and save nothing when the enterprise no longer exists at lock time"() {
		when:
		service.like(R1, CANDIDATE)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> false
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID ${R1} não existe"
		0 * likeRepository._
		0 * matchRepository._
	}

	def "like should save recruiter like, enterprise like and a match with each OPEN vacancy of the enterprise the candidate liked (B1)"() {
		when:
		service.like(R1, CANDIDATE)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> true

		then:
		1 * likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> false

		then:
		1 * likeRepository.insertRecruiterLike(connection, R1, CANDIDATE) >> true

		then:
		1 * likeRepository.insertEnterpriseLike(connection, ENTERPRISE, CANDIDATE) >> true

		then:
		1 * matchRepository.likedVacanciesOfEnterprise(connection, CANDIDATE, ENTERPRISE) >>
				[(V1): VacancyStatus.OPEN, (V2): VacancyStatus.OPEN]
		1 * matchRepository.insertMatch(connection, CANDIDATE, V1) >> true
		1 * matchRepository.insertMatch(connection, CANDIDATE, V2) >> true
		0 * matchRepository.insertMatch(*_)
		0 * matchRepository.likedVacanciesOfEnterprise(_, _, { it != ENTERPRISE })
	}

	def "like should save recruiter like and enterprise like without match when the candidate liked no vacancy of the enterprise (B2)"() {
		given:
		likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> false

		when:
		service.like(R1, CANDIDATE)

		then:
		1 * likeRepository.insertRecruiterLike(connection, R1, CANDIDATE) >> true
		1 * likeRepository.insertEnterpriseLike(connection, ENTERPRISE, CANDIDATE) >> true
		1 * matchRepository.likedVacanciesOfEnterprise(connection, CANDIDATE, ENTERPRISE) >> [:]
		0 * matchRepository.insertMatch(*_)
	}

	def "like by a second recruiter of the enterprise should save only the recruiter like (B3)"() {
		given:
		likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> true

		when:
		service.like(R2, CANDIDATE)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> true
		1 * likeRepository.insertRecruiterLike(connection, R2, CANDIDATE) >> true
		0 * likeRepository.insertEnterpriseLike(*_)
		0 * matchRepository._
	}

	def "like should not fail nor change anything when the recruiter already likes the candidate (B7)"() {
		given:
		likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> true

		when:
		service.like(R1, CANDIDATE)

		then:
		1 * likeRepository.insertRecruiterLike(connection, R1, CANDIDATE) >> false
		0 * likeRepository.insertEnterpriseLike(*_)
		0 * matchRepository._
		noExceptionThrown()
	}

	def "like should fail with 400 and save nothing when the recruiter has the candidate CPF #recruiterCpf / #candidateCpf (B9)"() {
		given:
		RecruiterRepository recruiters = Stub() {
			findById(R1) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE, cpf: recruiterCpf)
		}
		CandidateRepository candidates = Stub() {
			findById(CANDIDATE) >> validCandidate(id: CANDIDATE, cpf: candidateCpf)
		}
		RecruiterLikeService sameCpfService = new RecruiterLikeService(connectionManager, recruiters, candidates,
		likeRepository, matchRepository)

		when:
		sameCpfService.like(R1, CANDIDATE)

		then:
		ValidationException exception = thrown()
		exception.message == 'Você não pode curtir o seu próprio perfil de candidato.'
		!exception.message.contains('529')
		0 * likeRepository.insertRecruiterLike(*_)
		0 * likeRepository.insertEnterpriseLike(*_)
		0 * matchRepository.insertMatch(*_)
		0 * likeRepository._
		0 * matchRepository._

		where:
		recruiterCpf     | candidateCpf
		'529.982.247-25' | '529.982.247-25'
		'52998224725'    | '52998224725'
		'529.982.247-25' | '52998224725'
		'52998224725'    | '529.982.247-25'
	}

	def "like should fail with 404 and save nothing when the candidate does not exist (B10)"() {
		when:
		service.like(R1, 99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == 'Candidato com ID 99 não existe'
		0 * likeRepository._
		0 * matchRepository._
	}

	def "like should fail with 404 and save nothing when the recruiter does not exist"() {
		when:
		service.like(99, CANDIDATE)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == 'Recrutador com ID 99 não existe'
		0 * likeRepository._
		0 * matchRepository._
	}

	def "like by an HR recruiter should create the match with an IT vacancy, because the area does not filter (B13)"() {
		given:
		RecruiterRepository recruiters = Stub() {
			findById(R1) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE, area: RecruitmentArea.HR)
		}
		RecruiterLikeService hrService = new RecruiterLikeService(connectionManager, recruiters, candidateRepository,
		likeRepository, matchRepository)
		likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> false
		matchRepository.likedVacanciesOfEnterprise(connection, CANDIDATE, ENTERPRISE) >> [(V1): VacancyStatus.OPEN]

		when:
		hrService.like(R1, CANDIDATE)

		then:
		1 * matchRepository.insertMatch(connection, CANDIDATE, V1) >> true
	}

	def "like should create matches only with OPEN vacancies when liked vacancies are #liked (B14)"() {
		given:
		likeRepository.enterpriseLikeExists(connection, ENTERPRISE, CANDIDATE) >> false
		matchRepository.likedVacanciesOfEnterprise(connection, CANDIDATE, ENTERPRISE) >> liked

		when:
		service.like(R1, CANDIDATE)

		then:
		1 * likeRepository.insertRecruiterLike(connection, R1, CANDIDATE) >> true
		1 * likeRepository.insertEnterpriseLike(connection, ENTERPRISE, CANDIDATE) >> true
		interaction {
			matched.each { int vacancyId -> 1 * matchRepository.insertMatch(connection, CANDIDATE, vacancyId) >> true }
		}
		0 * matchRepository.insertMatch(*_)
		0 * likeRepository.deleteCandidateLike(*_)

		where:
		liked                                                    || matched
		[(V2): VacancyStatus.OPEN]                               || [V2]
		[(V1): VacancyStatus.PAUSED, (V2): VacancyStatus.CLOSED] || []
		[(V1): VacancyStatus.PAUSED, (V2): VacancyStatus.OPEN]   || [V2]
		[(V1): VacancyStatus.CLOSED, (V2): VacancyStatus.OPEN]   || [V2]
	}

	def "like should roll back everything when saving a match fails (B16)"() {
		given:
		Connection realConnection = Mock()
		ConnectionFactory connectionFactory = Stub() {
			createConnection() >> realConnection
		}
		RecruiterLikeService transactionalService = new RecruiterLikeService(new DatabaseConnection(connectionFactory),
		recruiterRepository, candidateRepository, likeRepository, matchRepository)
		likeRepository.enterpriseLikeExists(realConnection, ENTERPRISE, CANDIDATE) >> false
		matchRepository.likedVacanciesOfEnterprise(realConnection, CANDIDATE, ENTERPRISE) >>
				[(V1): VacancyStatus.OPEN, (V2): VacancyStatus.OPEN]

		when:
		transactionalService.like(R1, CANDIDATE)

		then:
		1 * likeRepository.insertRecruiterLike(realConnection, R1, CANDIDATE) >> true
		1 * likeRepository.insertEnterpriseLike(realConnection, ENTERPRISE, CANDIDATE) >> true
		1 * matchRepository.insertMatch(realConnection, CANDIDATE, V1) >> true
		1 * matchRepository.insertMatch(realConnection, CANDIDATE, V2) >> {
			throw new IllegalStateException('falha ao gravar o match')
		}
		thrown(IllegalStateException)
		0 * realConnection.commit()
		1 * realConnection.rollback()
	}
}
