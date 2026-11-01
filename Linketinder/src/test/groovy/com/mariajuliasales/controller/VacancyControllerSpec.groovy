package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.VacancyRequest
import com.mariajuliasales.dto.response.VacancyAnonymousResponse
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.VacancyRepository
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import com.mariajuliasales.service.validator.VacancyValidator
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validVacancy

class VacancyControllerSpec extends Specification {

	VacancyRepository vacancyRepository = Mock()
	EnterpriseRepository enterpriseRepository = Mock()
	VacancyController controller = new VacancyController(
	new VacancyService(vacancyRepository, new VacancyValidator()),
	new EnterpriseService(enterpriseRepository, new EnterpriseValidator(new PersonValidator())))

	private static VacancyRequest request(String status = null) {
		new VacancyRequest('Desenvolvedor Groovy', 'Desenvolvimento de APIs', 'Recife', 'PE', status, ['Groovy'])
	}

	def "create should attach the vacancy to the enterprise and return the response"() {
		given:
		Enterprise enterprise = validEnterprise(id: 2)
		enterpriseRepository.findById(2) >> enterprise

		when:
		VacancyAnonymousResponse response = controller.create(2, request())

		then:
		1 * vacancyRepository.create({ Vacancy vacancy -> vacancy.enterprise.is(enterprise) }) >>
		{ Vacancy vacancy -> vacancy.id = 8; vacancy }
		response.id() == 8
		response.status() == 'OPEN'
		enterprise.vacancies*.id == [8]
	}

	def "create should fail when the enterprise does not exist"() {
		given:
		enterpriseRepository.findById(99) >> null

		when:
		controller.create(99, request())

		then:
		thrown(EntityNotFoundException)
		0 * vacancyRepository.create(_)
	}

	def "update should keep the enterprise of the saved vacancy"() {
		given:
		Enterprise enterprise = validEnterprise(id: 2)
		vacancyRepository.findById(8) >> validVacancy(id: 8, enterprise: enterprise)

		when:
		VacancyAnonymousResponse response = controller.update(8, request('closed'))

		then:
		1 * vacancyRepository.update({ Vacancy vacancy ->
			vacancy.id == 8 && vacancy.enterprise.is(enterprise) && vacancy.status == VacancyStatus.CLOSED
		}) >> { Vacancy vacancy -> vacancy }
		response.status() == 'CLOSED'
	}

	def "statuses should list every vacancy status"() {
		expect:
		controller.findStatuses() == ['OPEN', 'CLOSED', 'PAUSED']
	}
}
