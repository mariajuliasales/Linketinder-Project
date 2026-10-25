package com.mariajuliasales.dto.response

record CandidateResponse(
		Integer id,
		String name,
		String email,
		AddressResponse address,
		String description,
		List<String> competences,
		String cpf,
		int age,
		String training
		) {
		}
