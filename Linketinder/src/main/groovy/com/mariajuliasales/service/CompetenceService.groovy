package com.mariajuliasales.service

import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Competence
import com.mariajuliasales.service.validator.CompetenceValidator

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound

class CompetenceService {

	private static final String ENTITY_NAME = "Competência"

	private final CompetenceDAO competenceDAO

	CompetenceService(CompetenceDAO competenceDAO) {
		this.competenceDAO = competenceDAO
	}

	Competence create(String name) {
		CompetenceValidator.validateName(name)
		ensureNameIsAvailable(name, null)
		competenceDAO.create(new Competence(name))
	}

	List<Competence> findAll() {
		competenceDAO.findAll()
	}

	Competence findById(int id) {
		requireFound(competenceDAO.findById(id), ENTITY_NAME, id)
	}

	Competence update(int id, String newName) {
		CompetenceValidator.validateName(newName)
		ensureNameIsAvailable(newName, id)
		requireFound(competenceDAO.update(new Competence(id, newName)), ENTITY_NAME, id)
	}

	void delete(int id) {
		if (!competenceDAO.delete(id)) {
			throw new EntityNotFoundException(ENTITY_NAME, id)
		}
	}

	private void ensureNameIsAvailable(String name, Integer currentId) {
		Competence existing = competenceDAO.findByName(name)
		if (existing && existing.id != currentId) {
			throw new DuplicateEntityException("Competência '${name.trim()}' já cadastrada".toString())
		}
	}
}
