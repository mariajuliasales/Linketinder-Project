package com.mariajuliasales.service

import com.mariajuliasales.dao.VacancyDAO
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.util.ValidateUtil

class VacancyService {

    private final VacancyDAO vacancyDAO

    VacancyService(VacancyDAO vacancyDAO) {
        this.vacancyDAO = vacancyDAO
    }

    Vacancy create(Vacancy vacancy) {
        validate(vacancy)
        Vacancy created = vacancyDAO.create(vacancy)
        created?.enterprise?.addVacancy(created)
        created
    }

    List<Vacancy> findAll() {
        vacancyDAO.findAll()
    }

    Vacancy findById(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("The job opening ID must be a positive integer.")
        }

        Vacancy vacancy = vacancyDAO.findById(id)

        if (!vacancy) {
            throw new IllegalArgumentException("No job opening found with the provided ID.")
        }

        vacancy
    }

    List<Vacancy> findByEnterprise(int enterpriseId) {
        vacancyDAO.findByEnterprise(enterpriseId)
    }

    Vacancy update(Vacancy vacancy) {
        validate(vacancy)
        Vacancy updated = vacancyDAO.update(vacancy)
        if (!updated) {
            throw new IllegalArgumentException("No job opening found with the provided ID.")
        }
        updated
    }

    void delete(int id) {
        if (!vacancyDAO.delete(id)) {
            throw new IllegalArgumentException("No job opening found with the provided ID.")
        }
    }

    private static void validate(Vacancy vacancy) {
        if (!vacancy.title || vacancy.title.blank) {
            throw new IllegalArgumentException("The job opening must have a title.")
        }

        if (!vacancy.competences || vacancy.competences.any { !(it instanceof Competence) }) {
            throw new IllegalArgumentException("The job opening must have at least one required skill.")
        }

        if (vacancy.enterprise == null) {
            throw new IllegalArgumentException("The job opening must be associated with a company.")
        }

        if (!vacancy.description?.trim()) {
            throw new IllegalArgumentException("The job opening must have a description.")
        }

        if (!ValidateUtil.isValidLocation(vacancy.address)) {
            throw new IllegalArgumentException("The job opening must have a valid city and state.")
        }
    }

}