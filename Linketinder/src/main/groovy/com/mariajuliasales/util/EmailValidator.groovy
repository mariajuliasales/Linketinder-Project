package com.mariajuliasales.util

final class EmailValidator {

	private static final String EMAIL_FORMAT = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$"

	static boolean isValid(String email) {
		email != null && email.matches(EMAIL_FORMAT)
	}
}