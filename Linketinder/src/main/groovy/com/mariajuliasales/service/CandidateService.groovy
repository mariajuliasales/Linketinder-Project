package com.mariajuliasales.service

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.service.validator.Validator

class CandidateService extends CrudService<Candidate> {

	CandidateService(CandidateRepository repository, Validator<Candidate> validator) {
		super(repository, validator, 'Candidato')
	}
}
