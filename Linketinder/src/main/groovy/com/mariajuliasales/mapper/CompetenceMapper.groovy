package com.mariajuliasales.mapper

import com.mariajuliasales.dto.response.CompetenceResponse
import com.mariajuliasales.model.Competence

final class CompetenceMapper {

	static List<Competence> toEntities(List<String> names) {
		names?.collect { String name -> new Competence(name) } ?: []
	}

	static List<String> toNames(List<Competence> competences) {
		competences?.collect { Competence competence -> competence.name } ?: []
	}

	static CompetenceResponse toResponse(Competence competence) {
		competence == null ? null : new CompetenceResponse(competence.id, competence.name)
	}

	static List<CompetenceResponse> toResponses(List<Competence> competences) {
		competences?.collect { Competence competence -> toResponse(competence) } ?: []
	}
}
