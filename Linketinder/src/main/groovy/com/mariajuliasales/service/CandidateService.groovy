package com.mariajuliasales.service

import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.service.validator.CandidateValidator

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound

class CandidateService {

	private static final String ENTITY_NAME = "Candidato"

	private final CandidateDAO candidateDAO

	CandidateService(CandidateDAO candidateDAO) {
		this.candidateDAO = candidateDAO
	}

	Candidate create(Candidate candidate) {
		CandidateValidator.validateForCreate(candidate)
		candidateDAO.create(candidate)
	}

	List<Candidate> findAll() {
		candidateDAO.findAll()
	}

	Candidate findById(int id) {
		requireFound(candidateDAO.findById(id), ENTITY_NAME, id)
	}

	Candidate update(Candidate candidate) {
		CandidateValidator.validateForUpdate(candidate)
		requireFound(candidateDAO.update(candidate), ENTITY_NAME, candidate.id)
	}

	void delete(int id) {
		if (!candidateDAO.delete(id)) {
			throw new EntityNotFoundException(ENTITY_NAME, id)
		}
	}
}
