package com.mariajuliasales.service

import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.service.validator.Validator

class EnterpriseService extends CrudService<Enterprise> {

	EnterpriseService(EnterpriseRepository repository, Validator<Enterprise> validator) {
		super(repository, validator, 'Empresa')
	}
}
