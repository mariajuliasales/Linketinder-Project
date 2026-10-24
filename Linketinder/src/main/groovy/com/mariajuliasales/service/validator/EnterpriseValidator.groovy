package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.util.CnpjValidator

import static com.mariajuliasales.service.validator.Validation.require

final class EnterpriseValidator {

	static void validateForCreate(Enterprise enterprise) {
		PersonValidator.validateForCreate(enterprise)
		validateEnterpriseFields(enterprise)
	}

	static void validateForUpdate(Enterprise enterprise) {
		PersonValidator.validateForUpdate(enterprise)
		validateEnterpriseFields(enterprise)
	}

	private static void validateEnterpriseFields(Enterprise enterprise) {
		require(CnpjValidator.isValid(enterprise.cnpj), "CNPJ inválido")
	}
}
