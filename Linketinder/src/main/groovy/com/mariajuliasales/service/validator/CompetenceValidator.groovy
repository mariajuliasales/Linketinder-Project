package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Competence

import static com.mariajuliasales.service.validator.Validation.hasText
import static com.mariajuliasales.service.validator.Validation.require

class CompetenceValidator implements Validator<Competence> {

	static final int MAX_NAME_LENGTH = 50

	@Override
	void validateForCreate(Competence competence) {
		validateName(competence?.name)
	}

	@Override
	void validateForUpdate(Competence competence) {
		validateName(competence?.name)
	}

	private static void validateName(String name) {
		require(hasText(name), "Nome da competência é obrigatório")
		require(name.trim().length() <= MAX_NAME_LENGTH,
				"Nome da competência deve ter no máximo ${MAX_NAME_LENGTH} caracteres".toString())
	}
}
