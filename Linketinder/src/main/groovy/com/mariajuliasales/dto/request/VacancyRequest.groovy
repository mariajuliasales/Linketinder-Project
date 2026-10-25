package com.mariajuliasales.dto.request

record VacancyRequest(
		String title,
		String description,
		String city,
		String state,
		String status,
		List<String> competences
		) {
		}
