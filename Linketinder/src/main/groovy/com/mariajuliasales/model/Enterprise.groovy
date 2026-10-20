package com.mariajuliasales.model

class Enterprise extends AbstractPerson {

	String cnpj
	List<Vacancy> vacancies = []

	void addVacancy(Vacancy vacancy) {
		if (!vacancy || vacancies.contains(vacancy)) {
			return
		}
		if (belongsToAnotherEnterprise(vacancy)) {
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

	private boolean belongsToAnotherEnterprise(Vacancy vacancy) {
		vacancy.enterprise != null && !vacancy.enterprise.is(this)
	}
}
