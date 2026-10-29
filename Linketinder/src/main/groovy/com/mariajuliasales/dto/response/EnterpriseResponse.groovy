package com.mariajuliasales.dto.response

record EnterpriseResponse(
		Integer id,
		String name,
		String email,
		AddressResponse address,
		String description,
		List<String> competences,
		String cnpj,
		List<VacancyAnonymousResponse> vacancies
		) {
		}
