package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.util.CpfValidator
import com.mariajuliasales.util.EmailValidator
import com.mariajuliasales.util.PasswordValidator

import java.time.LocalDate

import static com.mariajuliasales.service.validator.Validation.hasText
import static com.mariajuliasales.service.validator.Validation.require

/**
 * Não delega ao PersonValidator, porque ele exige endereço e o recrutador não tem.
 * Não valida endereço nem competências. As mensagens nunca repetem o valor recebido.
 */
class RecruiterValidator implements Validator<Recruiter> {

	static final int MIN_AGE = 18

	@Override
	void validateForCreate(Recruiter recruiter) {
		validateCommonFields(recruiter)
		validatePassword(recruiter.password)
		validateRecruiterFields(recruiter)
	}

	@Override
	void validateForUpdate(Recruiter recruiter) {
		validateCommonFields(recruiter)
		if (recruiter.password) {
			validatePassword(recruiter.password)
		}
		validateRecruiterFields(recruiter)
	}

	private static void validateCommonFields(Recruiter recruiter) {
		require(recruiter != null, "Dados não informados")
		require(hasText(recruiter.name), "Nome é obrigatório")
		require(EmailValidator.isValid(recruiter.email), "E-mail inválido")
	}

	private static void validatePassword(String password) {
		require(PasswordValidator.isValid(password),
				"A senha deve ter pelo menos ${PasswordValidator.MIN_LENGTH} caracteres".toString())
	}

	private static void validateRecruiterFields(Recruiter recruiter) {
		require(CpfValidator.isValid(recruiter.cpf), "CPF inválido")
		validateBirthDate(recruiter)
		require(recruiter.area != null, "Área de recrutamento inválida")
	}

	private static void validateBirthDate(Recruiter recruiter) {
		require(recruiter.birthDate != null, "Data de nascimento é obrigatória")
		require(!recruiter.birthDate.isAfter(LocalDate.now()), "Data de nascimento não pode estar no futuro")
		require(recruiter.age >= MIN_AGE, "O recrutador deve ter pelo menos ${MIN_AGE} anos".toString())
	}
}
