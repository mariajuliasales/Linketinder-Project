package com.mariajuliasales.service

import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.repository.Database

class VacancyService {

    private final Database dataBase;

    VacancyService(Database dataBase) {
        this.dataBase = dataBase
    }

    Vacancy create(Vacancy vacancy) {

        if (!vacancy.title || vacancy.title.blank) {
            throw new IllegalArgumentException("The job opening must have a title.")
        }

        if (!vacancy.competences || vacancy.competences.any { !(it instanceof Competence) }) {
            throw new IllegalArgumentException("The job opening must have at least one required skill.")
        }

        if (vacancy.enterprise == null) {
            throw new IllegalArgumentException("The job opening must be associated with a company.")
        }

        dataBase.createVacancy(vacancy)
    }

}