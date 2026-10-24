package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException

final class Validation {

	static void require(boolean condition, String message) {
		if (!condition) {
			throw new ValidationException(message)
		}
	}

	static boolean hasText(String value) {
		value != null && !value.isBlank()
	}
}
