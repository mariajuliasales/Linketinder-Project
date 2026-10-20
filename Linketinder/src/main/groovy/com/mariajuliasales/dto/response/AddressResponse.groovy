package com.mariajuliasales.dto.response

record AddressResponse(
		String cep,
		String street,
		String number,
		String complement,
		String neighborhood,
		String city,
		String state,
		String country
		) {
		}
