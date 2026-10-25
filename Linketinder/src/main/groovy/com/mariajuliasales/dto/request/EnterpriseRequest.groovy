package com.mariajuliasales.dto.request

record EnterpriseRequest(
		String name,
		String email,
		String password,
		AddressRequest address,
		String description,
		List<String> competences,
		String cnpj
		) {
		}
