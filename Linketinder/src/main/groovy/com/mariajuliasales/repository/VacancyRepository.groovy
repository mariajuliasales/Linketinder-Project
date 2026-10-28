package com.mariajuliasales.repository

import com.mariajuliasales.model.Vacancy

interface VacancyRepository extends Repository<Vacancy> {

	List<Vacancy> findByEnterprise(int enterpriseId)
}
