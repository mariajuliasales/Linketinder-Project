package com.mariajuliasales.mapper

import com.mariajuliasales.model.Competence

final class CompetenceMapper {

	static List<Competence> toEntities(List<String> names) {
		names?.collect { String name -> new Competence(name) } ?: []
	}

	static List<String> toNames(List<Competence> competences) {
		competences?.collect { Competence competence -> competence.name } ?: []
	}
}
