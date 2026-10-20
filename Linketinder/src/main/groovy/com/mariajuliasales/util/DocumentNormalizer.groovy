package com.mariajuliasales.util

final class DocumentNormalizer {

	static String onlyDigits(String value) {
		value?.replaceAll(/\D/, '')
	}

	static String normalizeCnpj(String cnpj) {
		cnpj?.replaceAll(/[.\-\/\s]/, '')?.toUpperCase()
	}
}
