package com.mariajuliasales.service

import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.repository.VacancyRepository
import com.mariajuliasales.service.validator.Validator

class VacancyService extends CrudService<Vacancy> {

	private final VacancyRepository vacancyRepository

	VacancyService(VacancyRepository repository, Validator<Vacancy> validator) {
		super(repository, validator, 'Vaga')
		this.vacancyRepository = repository
	}

	@Override
	Vacancy create(Vacancy vacancy) {
		Vacancy created = super.create(vacancy)
		created.enterprise.addVacancy(created)
		created
	}

	List<Vacancy> findByEnterprise(int enterpriseId) {
		vacancyRepository.findByEnterprise(enterpriseId)
	}
}
