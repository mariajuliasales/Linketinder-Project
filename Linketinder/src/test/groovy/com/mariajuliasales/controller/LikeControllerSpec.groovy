package com.mariajuliasales.controller

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.dto.response.CandidateMatchResponse
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.repository.VacancyRepository
import com.mariajuliasales.service.CandidateLikeService
import com.mariajuliasales.service.MatchQueryService
import com.mariajuliasales.service.RecruiterLikeService
import spock.lang.Specification

import java.sql.Connection

import static com.mariajuliasales.fixture.TestData.validCandidate
import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validRecruiter
import static com.mariajuliasales.fixture.TestData.validVacancy

class LikeControllerSpec extends Specification {

	private static final List<String> FORBIDDEN_FIELDS = ['cpf', 'birthDate', 'age', 'address', 'password',
		'passwordHash', 'likedVacancies', 'enterpriseId', 'cnpj']

	static final int E1 = 3
	static final int E2 = 4
	static final int R1 = 7
	static final int R2 = 8
	static final int C5 = 5
	static final int C6 = 6
	static final int V1 = 10
	static final int V2 = 11

	Connection connection = Stub()
	ConnectionManager connectionManager = Stub() {
		withConnection(_) >> { Closure work -> work(connection) }
		withTransaction(_) >> { Closure work -> work(connection) }
	}
	CandidateRepository candidateRepository = Mock() {
		findById(C5) >> validCandidate(id: C5, name: 'Lucas', email: 'lucas@email.com')
		findById(_) >> null
	}
	EnterpriseRepository enterpriseRepository = Stub() {
		findById(E1) >> validEnterprise(id: E1)
		findById(E2) >> validEnterprise(id: E2)
		findById(_) >> null
	}
	RecruiterRepository recruiterRepository = Stub() {
		findById(R1) >> validRecruiter(id: R1, enterpriseId: E1)
		findById(R2) >> validRecruiter(id: R2, enterpriseId: E2, cpf: '714.602.380-01')
		findById(_) >> null
	}
	VacancyRepository vacancyRepository = Stub() {
		findById(V1) >> validVacancy(id: V1, enterprise: validEnterprise(id: E1))
	}
	LikeRepository likeRepository = Mock()
	MatchRepository matchRepository = Mock()

	LikeController controller = new LikeController(
	new CandidateLikeService(connectionManager, candidateRepository, vacancyRepository, likeRepository, matchRepository),
	new RecruiterLikeService(connectionManager, recruiterRepository, candidateRepository, likeRepository, matchRepository),
	new MatchQueryService(connectionManager, candidateRepository, enterpriseRepository, recruiterRepository,
	matchRepository))

	private static Vacancy matchedVacancy(int id, String title) {
		new Vacancy(id: id, title: title, status: VacancyStatus.OPEN)
	}

	def "candidateLikes should delegate to the candidate like service"() {
		given:
		likeRepository.lockEnterprise(connection, E1) >> true
		likeRepository.lockVacancyStatus(connection, V1) >> VacancyStatus.OPEN

		when:
		controller.candidateLikes(C5, V1)

		then:
		1 * likeRepository.insertCandidateLike(connection, C5, V1)
	}

	def "candidateUnlikes should delegate to the candidate like service"() {
		when:
		controller.candidateUnlikes(C5, V1)

		then:
		1 * likeRepository.deleteCandidateLike(connection, C5, V1)
	}

	def "recruiterLikes should delegate to the recruiter like service"() {
		given:
		likeRepository.lockEnterprise(connection, E1) >> true
		likeRepository.enterpriseLikeExists(connection, E1, C5) >> true

		when:
		controller.recruiterLikes(R1, C5)

		then:
		1 * likeRepository.insertRecruiterLike(connection, R1, C5)
	}

	def "recruiterUnlikes should delegate to the recruiter like service"() {
		given:
		likeRepository.lockEnterprise(connection, E1) >> true

		when:
		controller.recruiterUnlikes(R1, C5)

		then:
		1 * likeRepository.deleteRecruiterLike(connection, R1, C5)
	}

	def "recruiter should see the matched candidate revealed with only the matched vacancy"() {
		given: 'C5 tem match só em V1 de E1 (V2 de E1 não tem match)'
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [matchedVacancy(V1, 'Full Stack')]]
		candidateRepository.findAllByIds(_) >> [validCandidate(id: C5, name: 'Lucas', email: 'lucas@email.com')]

		when:
		List<CandidateMatchResponse> responses = controller.listCandidatesForRecruiter(R1)

		then:
		responses.size() == 1
		with(responses.first()) {
			id() == C5
			name() == 'Lucas'
			email() == 'lucas@email.com'
			training() == 'Ciência da Computação'
			competences() == ['Java']
			matchedVacancies()*.id() == [V1]
			matchedVacancies()*.title() == ['Full Stack']
			matchedVacancies()*.status() == ['OPEN']
		}
	}

	def "enterprise should see the matched candidate revealed"() {
		given:
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [matchedVacancy(V1, 'Full Stack')]]
		candidateRepository.findAllByIds(_) >> [validCandidate(id: C5, name: 'Lucas', email: 'lucas@email.com')]

		when:
		List<CandidateMatchResponse> responses = controller.listCandidatesForEnterprise(E1)

		then:
		responses*.name() == ['Lucas']
		responses*.email() == ['lucas@email.com']
		responses.first().matchedVacancies()*.id() == [V1]
	}

	def "candidate without match, even with a one-sided like, should not appear in the reveal lists"() {
		given: 'C6 curtiu V1 de E1 sem match; só C5 tem vacancy_match com E1'
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [matchedVacancy(V1, 'Full Stack')]]
		candidateRepository.findAllByIds({ it as Set == [C5] as Set }) >> [validCandidate(id: C5)]

		when:
		List<CandidateMatchResponse> forRecruiter = controller.listCandidatesForRecruiter(R1)
		List<CandidateMatchResponse> forEnterprise = controller.listCandidatesForEnterprise(E1)

		then:
		forRecruiter*.id() == [C5]
		forEnterprise*.id() == [C5]
		!(C6 in forRecruiter*.id())
		!(C6 in forEnterprise*.id())
	}

	def "match with E1 should not reveal the candidate to E2 or its recruiters"() {
		given:
		matchRepository.matchedVacanciesByEnterprise(connection, E1) >> [(C5): [matchedVacancy(V1, 'Full Stack')]]
		matchRepository.matchedVacanciesByEnterprise(connection, E2) >> [:]

		expect:
		controller.listCandidatesForRecruiter(R2) == []
		controller.listCandidatesForEnterprise(E2) == []
	}

	def "reveal response should not carry sensitive fields"() {
		expect:
		CandidateMatchResponse.recordComponents*.name.intersect(FORBIDDEN_FIELDS).isEmpty()
	}

	def "missing recruiter or enterprise should throw not found"() {
		when:
		controller.listCandidatesForRecruiter(99)

		then:
		thrown(EntityNotFoundException)

		when:
		controller.listCandidatesForEnterprise(99)

		then:
		thrown(EntityNotFoundException)
	}
}
