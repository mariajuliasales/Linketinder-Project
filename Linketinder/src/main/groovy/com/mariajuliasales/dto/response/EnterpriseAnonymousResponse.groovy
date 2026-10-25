package com.mariajuliasales.dto.response

record EnterpriseAnonymousResponse(
		Integer id,
		String state,
		String country,
		String description,
		List<String> competences,
		int vacancyCount
		) {
		}
