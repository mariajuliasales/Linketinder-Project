package com.mariajuliasales.model

class Vacancy {

    int id
    String title
    String description
    List<Competence> competences = []
    VacancyStatus status = VacancyStatus.OPEN
    Address address
    Enterprise enterprise

    String viewVacancyAnonymous() {
        return "Vaga: ${id}, Título: ${title}, Descrição: ${description}, Local: ${address?.city}/${address?.state}, " +
                "Status: ${status}, Competências: ${competences.join(', ')}"
    }

}
