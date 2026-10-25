package com.mariajuliasales.service

import com.mariajuliasales.dao.VacancyDAO
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.service.validator.VacancyValidator

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound

class VacancyService {

	private static final String ENTITY_NAME = "Vaga"

	private final VacancyDAO vacancyDAO

	VacancyService(VacancyDAO vacancyDAO) {
		this.vacancyDAO = vacancyDAO
	}

	Vacancy create(Vacancy vacancy) {
		VacancyValidator.validate(vacancy)
		Vacancy created = vacancyDAO.create(vacancy)
		created.enterprise.addVacancy(created)
		created
	}

	List<Vacancy> findAll() {
		vacancyDAO.findAll()
	}

	Vacancy findById(int id) {
		requireFound(vacancyDAO.findById(id), ENTITY_NAME, id)
	}

	List<Vacancy> findByEnterprise(int enterpriseId) {
		vacancyDAO.findByEnterprise(enterpriseId)
	}

	Vacancy update(Vacancy vacancy) {
		VacancyValidator.validate(vacancy)
		requireFound(vacancyDAO.update(vacancy), ENTITY_NAME, vacancy.id)
	}

	void delete(int id) {
		if (!vacancyDAO.delete(id)) {
			throw new EntityNotFoundException(ENTITY_NAME, id)
		}
	}
}
