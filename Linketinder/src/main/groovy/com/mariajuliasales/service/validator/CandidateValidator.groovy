package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Person
import com.mariajuliasales.util.CpfValidator

import static com.mariajuliasales.service.validator.Validation.hasText
import static com.mariajuliasales.service.validator.Validation.require

class CandidateValidator implements Validator<Candidate> {

	private final Validator<Person> personValidator

	CandidateValidator(Validator<Person> personValidator) {
		this.personValidator = personValidator
	}

	@Override
	void validateForCreate(Candidate candidate) {
		personValidator.validateForCreate(candidate)
		validateCandidateFields(candidate)
	}

	@Override
	void validateForUpdate(Candidate candidate) {
		personValidator.validateForUpdate(candidate)
		validateCandidateFields(candidate)
	}

	private static void validateCandidateFields(Candidate candidate) {
		require(CpfValidator.isValid(candidate.cpf), "CPF inválido")
		require(candidate.birthDate != null, "Data de nascimento é obrigatória")
		require(hasText(candidate.training), "Formação é obrigatória")
	}
}
