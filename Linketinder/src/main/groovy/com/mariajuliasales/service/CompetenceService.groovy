package com.mariajuliasales.service

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.model.Competence
import com.mariajuliasales.repository.CompetenceRepository
import com.mariajuliasales.service.validator.Validator

class CompetenceService extends CrudService<Competence> implements CompetenceCatalog {

	private final CompetenceRepository competenceRepository

	CompetenceService(CompetenceRepository repository, Validator<Competence> validator) {
		super(repository, validator, 'Competência')
		this.competenceRepository = repository
	}

	@Override
	protected void checkBusinessRules(Competence competence) {
		Competence existing = competenceRepository.findByName(competence.name)
		if (existing && existing.id != competence.id) {
			throw new DuplicateEntityException("Competência '${competence.name}' já cadastrada".toString())
		}
	}
}
