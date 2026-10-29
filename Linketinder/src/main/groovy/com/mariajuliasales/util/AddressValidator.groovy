package com.mariajuliasales.util

import com.mariajuliasales.model.Address

final class AddressValidator {

	private static final String CEP_FORMAT = /^\d{5}-?\d{3}$/
	private static final String STATE_FORMAT = /^[A-Za-z]{2}$/

	static boolean isValidCep(String cep) {
		cep != null && cep ==~ CEP_FORMAT
	}

	static boolean isValidState(String state) {
		state != null && state ==~ STATE_FORMAT
	}

	static boolean isValidLocation(Address address) {
		address != null && address.city?.trim() && isValidState(address.state)
	}
}
