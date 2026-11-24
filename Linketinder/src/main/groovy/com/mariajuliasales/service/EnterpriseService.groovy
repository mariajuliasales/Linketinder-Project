package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.service.validator.Validator

import java.sql.Connection

class EnterpriseService extends CrudService<Enterprise> {

	private final EnterpriseRepository enterpriseRepository
	private final RecruiterRepository recruiterRepository
	private final ConnectionManager connectionManager

	EnterpriseService(EnterpriseRepository repository, Validator<Enterprise> validator,
			RecruiterRepository recruiterRepository, ConnectionManager connectionManager) {
		super(repository, validator, 'Empresa')
		this.enterpriseRepository = repository
		this.recruiterRepository = recruiterRepository
		this.connectionManager = connectionManager
	}

	/**
	 * Numa transação: primeiro apaga as person dos recrutadores da empresa (cascata para recruiter e recruiter_like),
	 * depois a empresa. Nessa ordem porque a cascata de enterprise apagaria recruiter e os ids das person se perderiam
	 * (spec 2.1, "Exclusão da empresa"). Qualquer erro desfaz tudo; empresa inexistente → 404.
	 */
	@Override
	void delete(int id) {
		List<Recruiter> recruiters = recruiterRepository.findAllByEnterprise(id)
		connectionManager.withTransaction { Connection connection ->
			recruiters.each { Recruiter recruiter -> recruiterRepository.delete(connection, recruiter.id) }
			if (!enterpriseRepository.delete(connection, id)) {
				throw new EntityNotFoundException(entityName, id)
			}
		}
	}
}
