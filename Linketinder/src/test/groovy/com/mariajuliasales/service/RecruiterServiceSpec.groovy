package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionFactory
import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.dao.DatabaseConnection
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.service.validator.RecruiterValidator
import spock.lang.Specification

import java.sql.Connection

import static com.mariajuliasales.fixture.TestData.validCandidate
import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validRecruiter

class RecruiterServiceSpec extends Specification {

	static final int R1 = 7
	static final int ENTERPRISE = 3
	static final int OTHER_ENTERPRISE = 4
	static final int CANDIDATE_C = 5
	static final int CANDIDATE_D = 6

	Connection connection = Stub()
	ConnectionManager connectionManager = Stub() {
		withTransaction(_) >> { Closure work -> work(connection) }
	}
	RecruiterRepository recruiterRepository = Mock()
	EnterpriseRepository enterpriseRepository = Mock()
	LikeRepository likeRepository = Mock()
	MatchRepository matchRepository = Mock()
	RecruiterService recruiterService = createService(connectionManager)

	private RecruiterService createService(ConnectionManager manager) {
		RecruiterLikeService recruiterLikeService = new RecruiterLikeService(manager, recruiterRepository,
		Stub(CandidateRepository), likeRepository, matchRepository)
		new RecruiterService(recruiterRepository, enterpriseRepository, new RecruiterValidator(), manager,
		likeRepository, recruiterLikeService)
	}

	def "create should save a valid recruiter linked to the enterprise"() {
		given:
		Recruiter recruiter = validRecruiter(id: null, enterpriseId: null)
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)

		when:
		Recruiter result = recruiterService.create(3, recruiter)

		then:
		1 * recruiterRepository.isEmailInUse("rafael.lopes@techsolutions.com.br") >> false
		1 * recruiterRepository.create({ Recruiter saved -> saved.enterpriseId == 3 }) >> { Recruiter saved ->
			saved.id = 10
			saved
		}
		result.id == 10
		result.enterpriseId == 3
	}

	def "create should use the enterprise from the parameter, not from the recruiter data"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)

		when:
		recruiterService.create(3, validRecruiter(enterpriseId: 7))

		then:
		1 * recruiterRepository.create({ Recruiter saved -> saved.enterpriseId == 3 }) >> { Recruiter saved -> saved }
	}

	def "create should fail with 409 when the email is already in use, ignoring case"() {
		given:
		enterpriseRepository.findById(1) >> validEnterprise()

		when:
		recruiterService.create(1, validRecruiter(email: "Ana@X.com"))

		then:
		1 * recruiterRepository.isEmailInUse("Ana@X.com") >> true
		DuplicateEntityException exception = thrown()
		exception.message == "E-mail já cadastrado"
		0 * recruiterRepository.create(_)
	}

	def "create should accept the CPF of an existing candidate"() {
		given:
		String candidateCpf = validCandidate().cpf
		enterpriseRepository.findById(1) >> validEnterprise()
		recruiterRepository.isEmailInUse(_) >> false

		when:
		recruiterService.create(1, validRecruiter(cpf: candidateCpf))

		then:
		notThrown(Exception)
		1 * recruiterRepository.create({ Recruiter saved -> saved.cpf == candidateCpf }) >> { Recruiter saved -> saved }
	}

	def "create should fail with 404 when the enterprise does not exist"() {
		given:
		enterpriseRepository.findById(9999) >> null

		when:
		recruiterService.create(9999, validRecruiter())

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 9999 não existe"
		0 * recruiterRepository.isEmailInUse(_)
		0 * recruiterRepository.create(_)
	}

	def "create should validate before checking the enterprise and the email"() {
		when:
		recruiterService.create(1, validRecruiter(name: " "))

		then:
		ValidationException exception = thrown()
		exception.message == "Nome é obrigatório"
		0 * enterpriseRepository.findById(_)
		0 * recruiterRepository.isEmailInUse(_)
		0 * recruiterRepository.create(_)
	}

	def "create should require a password"() {
		when:
		recruiterService.create(1, validRecruiter(password: ""))

		then:
		thrown(ValidationException)
		0 * recruiterRepository.create(_)
	}

	def "listByEnterprise should return only the recruiters of the enterprise (CA46)"() {
		given:
		enterpriseRepository.findById(ENTERPRISE) >> validEnterprise(id: ENTERPRISE)
		List<Recruiter> own = [validRecruiter(id: 7, enterpriseId: ENTERPRISE), validRecruiter(id: 8, enterpriseId: ENTERPRISE)]

		when:
		List<Recruiter> result = recruiterService.listByEnterprise(ENTERPRISE)

		then:
		1 * recruiterRepository.findAllByEnterprise(ENTERPRISE) >> own
		0 * recruiterRepository.findAllByEnterprise(OTHER_ENTERPRISE)
		result*.id == [7, 8]
	}

	def "listByEnterprise should fail with 404 when the enterprise does not exist"() {
		given:
		enterpriseRepository.findById(9999) >> null

		when:
		recruiterService.listByEnterprise(9999)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 9999 não existe"
		0 * recruiterRepository.findAllByEnterprise(_)
	}

	def "update own profile should keep the current password when the new one is #label"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(password: null)

		when:
		Recruiter result = recruiterService.updateOwnProfile(1, "Nova descrição", password)

		then:
		1 * recruiterRepository.updateProfile(1, "Nova descrição", password) >> true
		result.description == "Nova descrição"
		result.password == null

		where:
		label   | password
		"empty" | ""
		"null"  | null
	}

	def "update own profile should save a valid new password and return the recruiter without it"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(id: 1, password: null)

		when:
		Recruiter result = recruiterService.updateOwnProfile(1, "Nova descrição", "novaSenha")

		then:
		1 * recruiterRepository.updateProfile(1, "Nova descrição", "novaSenha") >> true
		result.id == 1
		result.description == "Nova descrição"
		result.password == null
	}

	def "update own profile should reject a short password with the same message as the validator"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(password: null)
		String validatorMessage = validatorMessageFor(validRecruiter(password: "123"))

		when:
		recruiterService.updateOwnProfile(1, "Nova descrição", "123")

		then:
		ValidationException exception = thrown()
		exception.message == "A senha deve ter pelo menos 6 caracteres"
		exception.message == validatorMessage
		0 * recruiterRepository.updateProfile(*_)
	}

	def "update own profile should validate only the new password, not the loaded record"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(cpf: "000.000.000-00", password: null)

		when:
		recruiterService.updateOwnProfile(1, "Nova descrição", "")

		then:
		notThrown(ValidationException)
		1 * recruiterRepository.updateProfile(1, "Nova descrição", "") >> true
	}

	private static String validatorMessageFor(Recruiter recruiter) {
		try {
			new RecruiterValidator().validateForUpdate(recruiter)
			return null
		} catch (ValidationException exception) {
			return exception.message
		}
	}

	def "update own profile should fail with 404 when the recruiter does not exist"() {
		given:
		recruiterRepository.findById(99) >> null

		when:
		recruiterService.updateOwnProfile(99, "Nova descrição", "")

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID 99 não existe"
		0 * recruiterRepository.updateProfile(*_)
	}

	def "update own profile should fail with 404 when the recruiter is removed before the update"() {
		given:
		recruiterRepository.findById(5) >> validRecruiter(id: 5, password: null)

		when:
		recruiterService.updateOwnProfile(5, "Nova descrição", "")

		then:
		1 * recruiterRepository.updateProfile(5, "Nova descrição", "") >> false
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID 5 não existe"
	}

	def "removeByEnterprise should fail with 404 and delete nothing when the recruiter belongs to another enterprise (CA14)"() {
		when:
		recruiterService.removeByEnterprise(OTHER_ENTERPRISE, R1)

		then:
		1 * recruiterRepository.findByIdAndEnterprise(R1, OTHER_ENTERPRISE) >> null
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID 7 não existe"
		0 * recruiterRepository.delete(*_)
		0 * likeRepository._
		0 * matchRepository._
	}

	def "removeByEnterprise should fail with 404 and delete nothing when the recruiter does not exist"() {
		when:
		recruiterService.removeByEnterprise(ENTERPRISE, 99)

		then:
		1 * recruiterRepository.findByIdAndEnterprise(99, ENTERPRISE) >> null
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID 99 não existe"
		0 * recruiterRepository.delete(*_)
		0 * likeRepository._
		0 * matchRepository._
	}

	def "removeByEnterprise should fail with 404 and delete nothing when the enterprise no longer exists at lock time"() {
		given:
		recruiterRepository.findByIdAndEnterprise(R1, ENTERPRISE) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)

		when:
		recruiterService.removeByEnterprise(ENTERPRISE, R1)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> false
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID 7 não existe"
		0 * recruiterRepository.delete(*_)
		0 * likeRepository._
		0 * matchRepository._
	}

	def "removeByEnterprise of the only recruiter liking the candidate should delete the enterprise like and the matches (CA15, B15)"() {
		given:
		recruiterRepository.findByIdAndEnterprise(R1, ENTERPRISE) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)

		when:
		recruiterService.removeByEnterprise(ENTERPRISE, R1)

		then:
		1 * likeRepository.lockEnterprise(connection, ENTERPRISE) >> true

		then:
		1 * likeRepository.candidatesLikedBy(connection, R1) >> [CANDIDATE_C]

		then:
		1 * likeRepository.deleteRecruiterLike(connection, R1, CANDIDATE_C) >> true

		then:
		1 * likeRepository.otherRecruiterOfEnterpriseLikes(connection, R1, CANDIDATE_C) >> false

		then:
		1 * likeRepository.deleteEnterpriseLike(connection, ENTERPRISE, CANDIDATE_C) >> true
		1 * matchRepository.deleteMatchesOfCandidateWithEnterprise(connection, CANDIDATE_C, ENTERPRISE) >> 1

		then:
		1 * recruiterRepository.delete(connection, R1) >> true
		0 * likeRepository._
		0 * matchRepository._
	}

	def "removeByEnterprise should keep the enterprise like and the matches when another recruiter still likes the candidate (CA16)"() {
		given:
		recruiterRepository.findByIdAndEnterprise(R1, ENTERPRISE) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)
		likeRepository.lockEnterprise(connection, ENTERPRISE) >> true
		likeRepository.candidatesLikedBy(connection, R1) >> [CANDIDATE_C]

		when:
		recruiterService.removeByEnterprise(ENTERPRISE, R1)

		then:
		1 * likeRepository.deleteRecruiterLike(connection, R1, CANDIDATE_C) >> true
		1 * likeRepository.otherRecruiterOfEnterpriseLikes(connection, R1, CANDIDATE_C) >> true
		0 * likeRepository.deleteEnterpriseLike(*_)
		0 * matchRepository._
		1 * recruiterRepository.delete(connection, R1) >> true
	}

	def "removeByEnterprise should delete only the enterprise like and matches of the candidate no other recruiter likes"() {
		given:
		recruiterRepository.findByIdAndEnterprise(R1, ENTERPRISE) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)
		likeRepository.lockEnterprise(connection, ENTERPRISE) >> true
		likeRepository.candidatesLikedBy(connection, R1) >> [CANDIDATE_C, CANDIDATE_D]
		likeRepository.deleteRecruiterLike(connection, R1, _) >> true
		likeRepository.otherRecruiterOfEnterpriseLikes(connection, R1, CANDIDATE_C) >> false
		likeRepository.otherRecruiterOfEnterpriseLikes(connection, R1, CANDIDATE_D) >> true

		when:
		recruiterService.removeByEnterprise(ENTERPRISE, R1)

		then:
		1 * likeRepository.deleteEnterpriseLike(connection, ENTERPRISE, CANDIDATE_C) >> true
		1 * matchRepository.deleteMatchesOfCandidateWithEnterprise(connection, CANDIDATE_C, ENTERPRISE) >> 1
		0 * likeRepository.deleteEnterpriseLike(connection, ENTERPRISE, CANDIDATE_D)
		0 * matchRepository.deleteMatchesOfCandidateWithEnterprise(connection, CANDIDATE_D, _)
		1 * recruiterRepository.delete(connection, R1) >> true
	}

	def "removeByEnterprise should delete the recruiter without touching enterprise likes when he liked no candidate"() {
		given:
		recruiterRepository.findByIdAndEnterprise(R1, ENTERPRISE) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)
		likeRepository.lockEnterprise(connection, ENTERPRISE) >> true
		likeRepository.candidatesLikedBy(connection, R1) >> []

		when:
		recruiterService.removeByEnterprise(ENTERPRISE, R1)

		then:
		0 * likeRepository.deleteRecruiterLike(*_)
		0 * likeRepository.deleteEnterpriseLike(*_)
		0 * matchRepository._
		1 * recruiterRepository.delete(connection, R1) >> true
	}

	def "removeByEnterprise should roll back the like and match deletions when deleting the recruiter fails"() {
		given:
		Connection realConnection = Mock()
		ConnectionFactory connectionFactory = Stub() {
			createConnection() >> realConnection
		}
		RecruiterService transactionalService = createService(new DatabaseConnection(connectionFactory))
		recruiterRepository.findByIdAndEnterprise(R1, ENTERPRISE) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)
		likeRepository.lockEnterprise(realConnection, ENTERPRISE) >> true
		likeRepository.candidatesLikedBy(realConnection, R1) >> [CANDIDATE_C]
		likeRepository.deleteRecruiterLike(realConnection, R1, CANDIDATE_C) >> true
		likeRepository.otherRecruiterOfEnterpriseLikes(realConnection, R1, CANDIDATE_C) >> false

		when:
		transactionalService.removeByEnterprise(ENTERPRISE, R1)

		then:
		1 * likeRepository.deleteEnterpriseLike(realConnection, ENTERPRISE, CANDIDATE_C) >> true
		1 * matchRepository.deleteMatchesOfCandidateWithEnterprise(realConnection, CANDIDATE_C, ENTERPRISE) >> 1
		1 * recruiterRepository.delete(realConnection, R1) >> { throw new IllegalStateException('falha ao apagar') }
		thrown(IllegalStateException)
		0 * realConnection.commit()
		1 * realConnection.rollback()
	}

	def "enterpriseIdOf should return the enterprise of the recruiter"() {
		given:
		recruiterRepository.findById(R1) >> validRecruiter(id: R1, enterpriseId: ENTERPRISE)

		expect:
		recruiterService.enterpriseIdOf(R1) == ENTERPRISE
	}

	def "enterpriseIdOf should fail with 404 when the recruiter does not exist"() {
		given:
		recruiterRepository.findById(9999) >> null

		when:
		recruiterService.enterpriseIdOf(9999)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == 'Recrutador com ID 9999 não existe'
	}
}
