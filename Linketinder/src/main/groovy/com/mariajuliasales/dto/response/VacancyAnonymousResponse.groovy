package com.mariajuliasales.dto.response

record VacancyAnonymousResponse(
		Integer id,
		String title,
		String description,
		String city,
		String state,
		String status,
		List<String> competences
		) {
		}
