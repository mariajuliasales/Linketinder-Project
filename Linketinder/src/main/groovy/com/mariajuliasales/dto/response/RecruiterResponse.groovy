package com.mariajuliasales.dto.response

/** Sem cpf, birthDate, age e password: nunca exibidos, nem na resposta do cadastro. */
record RecruiterResponse(
		Integer id,
		String name,
		String email,
		String area,
		String description
		) {
		}
