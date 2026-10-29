package com.mariajuliasales.util

final class CnpjValidator {

	private static final String CNPJ_FORMAT = /^[A-Za-z0-9]{2}\.?[A-Za-z0-9]{3}\.?[A-Za-z0-9]{3}\/?[A-Za-z0-9]{4}-?\d{2}$/
	private static final List<Integer> FIRST_DIGIT_WEIGHTS = [
		5,
		4,
		3,
		2,
		9,
		8,
		7,
		6,
		5,
		4,
		3,
		2
	]
	private static final List<Integer> SECOND_DIGIT_WEIGHTS = [
		6,
		5,
		4,
		3,
		2,
		9,
		8,
		7,
		6,
		5,
		4,
		3,
		2
	]

	static boolean isValid(String cnpj) {
		if (cnpj == null || !(cnpj ==~ CNPJ_FORMAT)) {
			return false
		}
		String normalized = DocumentNormalizer.normalizeCnpj(cnpj)
		!CheckDigit.isRepeatedSequence(normalized) && CheckDigit.matches(normalized, FIRST_DIGIT_WEIGHTS, SECOND_DIGIT_WEIGHTS)
	}
}
