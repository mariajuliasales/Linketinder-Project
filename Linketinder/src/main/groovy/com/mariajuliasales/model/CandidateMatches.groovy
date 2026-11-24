package com.mariajuliasales.model

/**
 * Candidato com as vagas de uma empresa que deram match com ele. Imutável: a lista de vagas é copiada.
 * Uso interno (service → mapper); nunca é serializado.
 */
record CandidateMatches(Candidate candidate, List<Vacancy> vacancies) {

	CandidateMatches {
		vacancies = List.copyOf(vacancies ?: [])
	}
}
