package com.mariajuliasales.dto.response

/** name, email e matchedVacancies só são preenchidos quando há match com a empresa. */
record CandidateMatchResponse(
		Integer id,
		String training,
		String description,
		List<String> competences,
		String name,
		String email,
		List<MatchedVacancyResponse> matchedVacancies
		) {
		}
