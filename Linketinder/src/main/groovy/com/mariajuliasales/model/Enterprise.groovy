package com.mariajuliasales.model

class Enterprise extends PersonAbstract {

    String cnpj
    List<Vacancy> vacancies = []

    void addVacancy(Vacancy vacancy) {
        if (!vacancy || vacancies.contains(vacancy)) {
            return
        }
        if (vacancy.enterprise != null && !vacancy.enterprise.is(this)) {
            vacancy.enterprise.removeVacancy(vacancy)
        }
        vacancies << vacancy
        vacancy.enterprise = this
    }

    void removeVacancy(Vacancy vacancy) {
        if (!vacancy || !vacancies.remove(vacancy)) {
            return
        }
        if (vacancy.enterprise.is(this)) {
            vacancy.enterprise = null
        }
    }

    @Override
    String viewProfileAnonymous() {
        "Perfil da Empresa ${id}: " +
                "Estado: ${address?.state} | País: ${address?.country} | Descrição da empresa: ${description} | " +
                "Competências: ${competences.join(', ')} | Vagas: ${vacancies.size()}"
    }

}
