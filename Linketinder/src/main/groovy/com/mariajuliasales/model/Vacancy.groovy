package com.mariajuliasales.model

class Vacancy {

	Integer id
	String title
	String description
	List<Competence> competences = []
	VacancyStatus status = VacancyStatus.OPEN
	Address address
	Enterprise enterprise
}
