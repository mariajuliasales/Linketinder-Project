package com.mariajuliasales.dto.request

import java.time.LocalDate

record RecruiterRequest(
		String name,
		String email,
		String password,
		String description,
		String cpf,
		LocalDate birthDate,
		String area
		) {
		}
