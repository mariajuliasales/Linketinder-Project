package com.mariajuliasales.util

final class PasswordValidator {

	static final int MIN_LENGTH = 6

	static boolean isValid(String password) {
		password != null && password.length() >= MIN_LENGTH
	}
}