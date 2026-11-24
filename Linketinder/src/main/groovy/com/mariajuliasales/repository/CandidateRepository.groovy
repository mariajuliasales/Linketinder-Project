package com.mariajuliasales.repository

import com.mariajuliasales.model.Candidate

interface CandidateRepository extends PersonRepository<Candidate> {

	Candidate findByCpf(String cpf)

	List<Candidate> findByCompetence(String competenceName)

	/** Candidatos com os ids informados, em ordem crescente de id. Coleção vazia ou nula → [] sem ir ao banco. */
	List<Candidate> findAllByIds(Collection<Integer> ids)
}
