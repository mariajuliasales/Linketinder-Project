package com.mariajuliasales.repository

import com.mariajuliasales.model.Competence

interface CompetenceRepository extends Repository<Competence> {

	Competence findByName(String name)
}
