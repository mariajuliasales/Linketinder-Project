package com.mariajuliasales.model

import groovy.transform.Canonical

@Canonical
class Vacancy {

    int id
    String title
    String description
    List<Competence> competences = []
    VacancyStatus status = VacancyStatus.OPEN
    Enterprise enterprise

    Vacancy (int id, String title, String description, List<Competence> competences, Enterprise enterprise) {
        this.id = id
        this.title = title
        this.description = description
        this.competences = competences
        this.enterprise = enterprise
    }

    String viewVacancyAnonymous() {
        return "Vaga: ${id}, Título: ${title}, Descrição: ${description}, Competências: ${competences.join(', ')}"
    }

}