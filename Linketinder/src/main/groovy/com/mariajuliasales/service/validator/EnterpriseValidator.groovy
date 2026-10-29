package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Person
import com.mariajuliasales.util.CnpjValidator

import static com.mariajuliasales.service.validator.Validation.require

class EnterpriseValidator implements Validator<Enterprise> {

	private final Validator<Person> personValidator

	EnterpriseValidator(Validator<Person> personValidator) {
		this.personValidator = personValidator
	}

	@Override
	void validateForCreate(Enterprise enterprise) {
		personValidator.validateForCreate(enterprise)
		validateEnterpriseFields(enterprise)
	}

	@Override
	void validateForUpdate(Enterprise enterprise) {
		personValidator.validateForUpdate(enterprise)
		validateEnterpriseFields(enterprise)
	}

	private static void validateEnterpriseFields(Enterprise enterprise) {
		require(CnpjValidator.isValid(enterprise.cnpj), "CNPJ inválido")
	}
}
