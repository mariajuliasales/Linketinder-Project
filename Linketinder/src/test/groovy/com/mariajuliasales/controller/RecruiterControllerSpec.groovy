package com.mariajuliasales.controller

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.dto.request.RecruiterRequest
import com.mariajuliasales.dto.response.RecruiterResponse
import com.mariajuliasales.dto.response.VacancyAnonymousResponse
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.model.RecruitmentArea
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.repository.VacancyRepository
import com.mariajuliasales.service.RecruiterLikeService
import com.mariajuliasales.service.RecruiterService
import com.mariajuliasales.service.VacancyService
import com.mariajuliasales.service.validator.RecruiterValidator
import com.mariajuliasales.service.validator.VacancyValidator
import spock.lang.Specification

import java.sql.Connection
import java.time.LocalDate

import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validRecruiter
import static com.mariajuliasales.fixture.TestData.validVacancy

class RecruiterControllerSpec extends Specification {

	private static final List<String> FORBIDDEN_FIELDS = ['cpf', 'birthDate', 'age', 'password', 'enterpriseId']

	Connection connection = Stub()
	ConnectionManager connectionManager = Stub() {
		withTransaction(_) >> { Closure work -> work(connection) }
	}
	RecruiterRepository recruiterRepository = Mock()
	EnterpriseRepository enterpriseRepository = Mock()
	LikeRepository likeRepository = Mock()
	VacancyRepository vacancyRepository = Mock()
	RecruiterController controller = new RecruiterController(new RecruiterService(recruiterRepository,
	enterpriseRepository, new RecruiterValidator(), connectionManager, likeRepository,
	new RecruiterLikeService(connectionManager, recruiterRepository, Stub(CandidateRepository), likeRepository,
	Stub(MatchRepository))), new VacancyService(vacancyRepository, new VacancyValidator()))

	private static RecruiterRequest request(Map changes = [:]) {
		Map data = [name: 'Rafael Teixeira Lopes', email: 'rafael.lopes@techsolutions.com.br', password: '123456',
			description: 'Recrutador de tecnologia', cpf: '301.958.476-01', birthDate: LocalDate.of(1988, 9, 3),
			area: 'ti'] + changes
		new RecruiterRequest(data.name, data.email, data.password, data.description, data.cpf, data.birthDate,
				data.area)
	}

	def "create should save the recruiter linked to the enterprise and return only the public fields (CA06, CA44)"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)
		recruiterRepository.isEmailInUse(_) >> false

		when:
		RecruiterResponse response = controller.create(3, request())

		then:
		1 * recruiterRepository.create({ Recruiter saved ->
			saved.enterpriseId == 3 && saved.area == RecruitmentArea.IT && saved.cpf == '301.958.476-01'
		}) >> { Recruiter saved -> saved.id = 10; saved }
		response == new RecruiterResponse(10, 'Rafael Teixeira Lopes', 'rafael.lopes@techsolutions.com.br', 'TI',
				'Recrutador de tecnologia')
		RecruiterResponse.recordComponents*.name.intersect(FORBIDDEN_FIELDS).isEmpty()
	}

	def "create should fail with 409 when the email is already used by any person, ignoring case (CA07)"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)
		recruiterRepository.isEmailInUse('RAFAEL.LOPES@techsolutions.com.br') >> true

		when:
		controller.create(3, request(email: 'RAFAEL.LOPES@techsolutions.com.br'))

		then:
		DuplicateEntityException exception = thrown()
		exception.message == 'E-mail já cadastrado'
		0 * recruiterRepository.create(_)
	}

	def "create should fail with 400 when the CPF is invalid (CA08)"() {
		when:
		controller.create(3, request(cpf: '111.111.111-11'))

		then:
		ValidationException exception = thrown()
		exception.message == 'CPF inválido'
		0 * recruiterRepository.create(_)
	}

	def "create should fail with 404 when the enterprise does not exist (CA13)"() {
		given:
		enterpriseRepository.findById(9999) >> null

		when:
		controller.create(9999, request())

		then:
		EntityNotFoundException exception = thrown()
		exception.message == 'Empresa com ID 9999 não existe'
		0 * recruiterRepository.create(_)
	}

	def "removeByEnterprise should answer 404 and delete nothing for a recruiter of another enterprise (CA14)"() {
		given:
		recruiterRepository.findByIdAndEnterprise(7, 4) >> null

		when:
		controller.removeByEnterprise(4, 7)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == 'Recrutador com ID 7 não existe'
		0 * recruiterRepository.delete(_, _)
		0 * likeRepository._
	}

	def "removeByEnterprise should delete the recruiter of the enterprise"() {
		given:
		recruiterRepository.findByIdAndEnterprise(7, 3) >> validRecruiter(id: 7, enterpriseId: 3)
		likeRepository.lockEnterprise(connection, 3) >> true
		likeRepository.candidatesLikedBy(connection, 7) >> []

		when:
		controller.removeByEnterprise(3, 7)

		then:
		1 * recruiterRepository.delete(connection, 7) >> true
	}

	def "updateOwnProfile should save the description and return the recruiter without sensitive fields (CA17)"() {
		given:
		recruiterRepository.findById(7) >> validRecruiter(id: 7, password: null)

		when:
		RecruiterResponse response = controller.updateOwnProfile(7, 'Nova descrição', '')

		then:
		1 * recruiterRepository.updateProfile(7, 'Nova descrição', '') >> true
		response.id() == 7
		response.description() == 'Nova descrição'
	}

	def "updateOwnProfile should reject a short password (CA17)"() {
		given:
		recruiterRepository.findById(7) >> validRecruiter(id: 7)

		when:
		controller.updateOwnProfile(7, 'Nova descrição', '123')

		then:
		thrown(ValidationException)
		0 * recruiterRepository.updateProfile(_, _, _)
	}

	def "listByEnterprise should return only the recruiters of the enterprise as responses (CA46)"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)
		recruiterRepository.findAllByEnterprise(3) >> [validRecruiter(id: 7, enterpriseId: 3),
			validRecruiter(id: 8, enterpriseId: 3, email: 'outra@techsolutions.com.br')]

		when:
		List<RecruiterResponse> responses = controller.listByEnterprise(3)

		then:
		responses*.id() == [7, 8]
		responses.every { it instanceof RecruiterResponse }
		responses*.email() == ['rafael.lopes@techsolutions.com.br', 'outra@techsolutions.com.br']
	}

	def "listByEnterprise should fail with 404 when the enterprise does not exist"() {
		given:
		enterpriseRepository.findById(9999) >> null

		when:
		controller.listByEnterprise(9999)

		then:
		thrown(EntityNotFoundException)
		0 * recruiterRepository.findAllByEnterprise(_)
	}

	def "listEnterpriseVacancies should return every vacancy of the recruiter enterprise in any status (CA19, CA21)"() {
		given:
		recruiterRepository.findById(7) >> validRecruiter(id: 7, enterpriseId: 3)

		when:
		List<VacancyAnonymousResponse> responses = controller.listEnterpriseVacancies(7)

		then:
		1 * vacancyRepository.findByEnterprise(3) >> [
			validVacancy(id: 1, status: VacancyStatus.OPEN, enterprise: validEnterprise(id: 3)),
			validVacancy(id: 2, status: VacancyStatus.PAUSED, enterprise: validEnterprise(id: 3)),
			validVacancy(id: 3, status: VacancyStatus.CLOSED, enterprise: validEnterprise(id: 3))
		]
		0 * vacancyRepository.findByEnterprise({ it != 3 })
		responses*.id() == [1, 2, 3]
		responses*.status() == ['OPEN', 'PAUSED', 'CLOSED']
		responses.every { it instanceof VacancyAnonymousResponse }
	}

	def "listEnterpriseVacancies should not expose any enterprise data (anonimato)"() {
		given:
		recruiterRepository.findById(7) >> validRecruiter(id: 7, enterpriseId: 3)
		vacancyRepository.findByEnterprise(3) >> [validVacancy(enterprise: validEnterprise(id: 3))]

		when:
		List<VacancyAnonymousResponse> responses = controller.listEnterpriseVacancies(7)

		then:
		VacancyAnonymousResponse.recordComponents*.name.intersect(
				['enterprise', 'enterpriseId', 'name', 'email', 'cnpj']).isEmpty()
		!responses.first().toString().contains('TechSolutions')
		!responses.first().toString().contains('11.222.333/0001-81')
	}

	def "listEnterpriseVacancies should fail with 404 and read no vacancy when the recruiter does not exist"() {
		given:
		recruiterRepository.findById(9999) >> null

		when:
		controller.listEnterpriseVacancies(9999)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == 'Recrutador com ID 9999 não existe'
		0 * vacancyRepository._
	}

	def "no vacancy create, update or delete operation should receive a recruiter (CA20)"() {
		expect:
		RecruiterController.declaredMethods*.name.findAll { it =~ /(?i)vacanc/ } == ['listEnterpriseVacancies']
		[VacancyService, VacancyController].every { Class type ->
			type.declaredMethods.every { method ->
				!method.parameterTypes.any { it == Recruiter || it == RecruiterRequest }
			}
		}
	}
}
