package com.mariajuliasales.util

final class CheckDigit {

	private static final int MODULUS = 11

	static boolean matches(String document, List<Integer> firstDigitWeights, List<Integer> secondDigitWeights) {
		String base = document.substring(0, firstDigitWeights.size())
		int firstDigit = calculate(base, firstDigitWeights)
		int secondDigit = calculate(base + firstDigit, secondDigitWeights)
		document.endsWith("${firstDigit}${secondDigit}")
	}

	static boolean isRepeatedSequence(String document) {
		document.toSet().size() == 1
	}

	private static int calculate(String base, List<Integer> weights) {
		int sum = 0
		base.eachWithIndex { String character, int index ->
			sum += characterValue(character) * weights[index]
		}
		int remainder = sum % MODULUS
		remainder < 2 ? 0 : MODULUS - remainder
	}

	private static int characterValue(String character) {
		(character as char) - ('0' as char)
	}
}
