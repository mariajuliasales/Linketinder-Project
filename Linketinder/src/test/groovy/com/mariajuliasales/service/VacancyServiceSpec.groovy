package com.mariajuliasales.service

import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.repository.VacancyRepository
import com.mariajuliasales.service.validator.VacancyValidator
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validVacancy

class VacancyServiceSpec extends Specification {

	VacancyRepository vacancyRepository = Mock()
	VacancyService vacancyService = new VacancyService(vacancyRepository, new VacancyValidator())

	def "create should save a valid vacancy"() {
		given:
		Vacancy vacancy = validVacancy()

		when:
		Vacancy result = vacancyService.create(vacancy)

		then:
		1 * vacancyRepository.create(vacancy) >> vacancy
		result == vacancy
	}

	def "create should add the vacancy to its enterprise"() {
		given:
		Enterprise enterprise = validEnterprise()
		Vacancy vacancy = validVacancy(enterprise: enterprise)
		vacancyRepository.create(vacancy) >> vacancy

		when:
		vacancyService.create(vacancy)

		then:
		enterprise.vacancies == [vacancy]
		vacancy.enterprise.is(enterprise)
	}

	def "create should not save an invalid vacancy"() {
		when:
		vacancyService.create(validVacancy(title: ""))

		then:
		thrown(ValidationException)
		0 * vacancyRepository.create(_)
	}

	def "find all should return the vacancies from the repository"() {
		given:
		List<Vacancy> vacancies = [
			validVacancy(id: 1),
			validVacancy(id: 2)
		]
		vacancyRepository.findAll() >> vacancies

		expect:
		vacancyService.findAll() == vacancies
	}

	def "find all should return an empty list when there are no vacancies"() {
		given:
		vacancyRepository.findAll() >> []

		expect:
		vacancyService.findAll().isEmpty()
	}

	def "find by id should return the vacancy from the repository"() {
		given:
		Vacancy vacancy = validVacancy(id: 10)
		vacancyRepository.findById(10) >> vacancy

		expect:
		vacancyService.findById(10) == vacancy
	}

	def "find by id should fail when the vacancy does not exist"() {
		given:
		vacancyRepository.findById(99) >> null

		when:
		vacancyService.findById(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Vaga com ID 99 não existe"
	}

	def "find by enterprise should return the enterprise vacancies"() {
		given:
		List<Vacancy> vacancies = [validVacancy()]
		vacancyRepository.findByEnterprise(1) >> vacancies

		expect:
		vacancyService.findByEnterprise(1) == vacancies
	}

	def "update should save a valid vacancy"() {
		given:
		Vacancy vacancy = validVacancy()

		when:
		Vacancy result = vacancyService.update(vacancy)

		then:
		1 * vacancyRepository.update(vacancy) >> vacancy
		result == vacancy
	}

	def "update should not save an invalid vacancy"() {
		when:
		vacancyService.update(validVacancy(competences: []))

		then:
		thrown(ValidationException)
		0 * vacancyRepository.update(_)
	}

	def "update should fail when the vacancy does not exist"() {
		given:
		vacancyRepository.update(_) >> null

		when:
		vacancyService.update(validVacancy(id: 99))

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Vaga com ID 99 não existe"
	}

	def "delete should remove the vacancy through the repository"() {
		when:
		vacancyService.delete(1)

		then:
		1 * vacancyRepository.delete(1) >> true
	}

	def "delete should fail when the vacancy does not exist"() {
		given:
		vacancyRepository.delete(99) >> false

		when:
		vacancyService.delete(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Vaga com ID 99 não existe"
	}
}
