package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Person
import com.mariajuliasales.util.AddressValidator
import com.mariajuliasales.util.EmailValidator
import com.mariajuliasales.util.PasswordValidator

import static com.mariajuliasales.service.validator.Validation.hasText
import static com.mariajuliasales.service.validator.Validation.require

final class PersonValidator {

	static void validateForCreate(Person person) {
		validateCommonFields(person)
		validatePassword(person.password)
	}

	static void validateForUpdate(Person person) {
		validateCommonFields(person)
		if (person.password) {
			validatePassword(person.password)
		}
	}

	private static void validateCommonFields(Person person) {
		require(person != null, "Dados não informados")
		require(hasText(person.name), "Nome é obrigatório")
		require(EmailValidator.isValid(person.email), "E-mail inválido")
		validateAddress(person.address)
	}

	private static void validatePassword(String password) {
		require(PasswordValidator.isValid(password),
				"A senha deve ter pelo menos ${PasswordValidator.MIN_LENGTH} caracteres".toString())
	}

	private static void validateAddress(Address address) {
		require(address != null, "Endereço é obrigatório")
		require(AddressValidator.isValidCep(address.cep), "CEP inválido")
		require(AddressValidator.isValidLocation(address), "Cidade e UF são obrigatórias")
	}
}
