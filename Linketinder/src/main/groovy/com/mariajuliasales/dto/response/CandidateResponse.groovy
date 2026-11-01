package com.mariajuliasales.dto.response

import java.time.LocalDate

record CandidateResponse(
		Integer id,
		String name,
		String email,
		AddressResponse address,
		String description,
		List<String> competences,
		String cpf,
		LocalDate birthDate,
		int age,
		String training
		) {
		}
