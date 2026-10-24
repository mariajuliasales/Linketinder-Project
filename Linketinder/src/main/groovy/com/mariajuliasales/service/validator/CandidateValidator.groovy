package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.util.CpfValidator

import static com.mariajuliasales.service.validator.Validation.hasText
import static com.mariajuliasales.service.validator.Validation.require

final class CandidateValidator {

	static void validateForCreate(Candidate candidate) {
		PersonValidator.validateForCreate(candidate)
		validateCandidateFields(candidate)
	}

	static void validateForUpdate(Candidate candidate) {
		PersonValidator.validateForUpdate(candidate)
		validateCandidateFields(candidate)
	}

	private static void validateCandidateFields(Candidate candidate) {
		require(CpfValidator.isValid(candidate.cpf), "CPF inválido")
		require(candidate.birthDate != null, "Data de nascimento é obrigatória")
		require(hasText(candidate.training), "Formação é obrigatória")
	}
}
