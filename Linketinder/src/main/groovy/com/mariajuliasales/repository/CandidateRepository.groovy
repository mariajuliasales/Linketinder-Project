package com.mariajuliasales.repository

import com.mariajuliasales.model.Candidate

interface CandidateRepository extends PersonRepository<Candidate> {

	Candidate findByCpf(String cpf)

	List<Candidate> findByCompetence(String competenceName)
}
