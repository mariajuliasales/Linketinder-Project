package com.mariajuliasales.controller

import com.mariajuliasales.dto.response.CompetenceResponse
import com.mariajuliasales.mapper.CompetenceMapper
import com.mariajuliasales.model.Competence
import com.mariajuliasales.service.CompetenceService

class CompetenceController implements CompetenceCatalog {

	private final CompetenceService competenceService

	CompetenceController(CompetenceService competenceService) {
		this.competenceService = competenceService
	}

	@Override
	List<CompetenceResponse> findAll() {
		CompetenceMapper.toResponses(competenceService.findAll())
	}

	CompetenceResponse findById(int id) {
		CompetenceMapper.toResponse(competenceService.findById(id))
	}

	CompetenceResponse create(String name) {
		CompetenceMapper.toResponse(competenceService.create(new Competence(name)))
	}

	CompetenceResponse update(int id, String name) {
		CompetenceMapper.toResponse(competenceService.update(new Competence(id, name)))
	}

	void delete(int id) {
		competenceService.delete(id)
	}
}
