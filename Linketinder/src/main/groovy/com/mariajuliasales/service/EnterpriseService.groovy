package com.mariajuliasales.service

import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.service.validator.EnterpriseValidator

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound

class EnterpriseService {

	private static final String ENTITY_NAME = "Empresa"

	private final EnterpriseDAO enterpriseDAO

	EnterpriseService(EnterpriseDAO enterpriseDAO) {
		this.enterpriseDAO = enterpriseDAO
	}

	Enterprise create(Enterprise enterprise) {
		EnterpriseValidator.validateForCreate(enterprise)
		enterpriseDAO.create(enterprise)
	}

	List<Enterprise> findAll() {
		enterpriseDAO.findAll()
	}

	Enterprise findById(int id) {
		requireFound(enterpriseDAO.findById(id), ENTITY_NAME, id)
	}

	Enterprise update(Enterprise enterprise) {
		EnterpriseValidator.validateForUpdate(enterprise)
		requireFound(enterpriseDAO.update(enterprise), ENTITY_NAME, enterprise.id)
	}

	void delete(int id) {
		if (!enterpriseDAO.delete(id)) {
			throw new EntityNotFoundException(ENTITY_NAME, id)
		}
	}
}
