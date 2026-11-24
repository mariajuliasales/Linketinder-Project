package com.mariajuliasales.dto.response

/** enterpriseName e enterpriseEmail só são preenchidos quando há match com a vaga. Sem enterpriseId. */
record VacancyMatchResponse(
		Integer id,
		String title,
		String description,
		String city,
		String state,
		String status,
		List<String> competences,
		String enterpriseName,
		String enterpriseEmail
		) {
		}
