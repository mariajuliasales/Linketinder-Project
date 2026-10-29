package com.mariajuliasales.util

final class CpfValidator {

	private static final String CPF_FORMAT = /^\d{3}\.?\d{3}\.?\d{3}-?\d{2}$/
	private static final List<Integer> FIRST_DIGIT_WEIGHTS = (10..2).toList()
	private static final List<Integer> SECOND_DIGIT_WEIGHTS = (11..2).toList()

	static boolean isValid(String cpf) {
		if (cpf == null || !(cpf ==~ CPF_FORMAT)) {
			return false
		}
		String digits = DocumentNormalizer.onlyDigits(cpf)
		!CheckDigit.isRepeatedSequence(digits) && CheckDigit.matches(digits, FIRST_DIGIT_WEIGHTS, SECOND_DIGIT_WEIGHTS)
	}
}
