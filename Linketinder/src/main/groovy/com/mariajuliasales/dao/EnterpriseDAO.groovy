package com.mariajuliasales.dao

import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.PersonType
import com.mariajuliasales.util.DocumentNormalizer

import java.sql.Connection
import java.sql.ResultSet

class EnterpriseDAO extends PersonDAO<Enterprise> {

	private static final String SELECT_ENTERPRISE = """
            SELECT enterprise.id, enterprise.cnpj, ${PERSON_COLUMNS}
            FROM enterprise
            JOIN person p  ON p.id = enterprise.person_id
            JOIN address a ON a.id = p.address_id"""
	private static final String INSERT_ENTERPRISE = 'INSERT INTO enterprise (person_id, cnpj) VALUES (?, ?) RETURNING id'
	private static final String UPDATE_ENTERPRISE = 'UPDATE enterprise SET cnpj = ? WHERE id = ?'

	private final VacancyDAO vacancyDAO

	EnterpriseDAO(DatabaseConnection database, CompetenceDAO competenceDAO, VacancyDAO vacancyDAO) {
		super(database, competenceDAO)
		this.vacancyDAO = vacancyDAO
	}

	@Override
	protected String getTableName() {
		'enterprise'
	}

	@Override
	protected PersonType getPersonType() {
		PersonType.ENTERPRISE
	}

	@Override
	protected CompetenceLink getCompetenceLink() {
		CompetenceLink.ENTERPRISE
	}

	@Override
	protected String getSelectSql() {
		SELECT_ENTERPRISE
	}

	@Override
	protected Enterprise mapRow(ResultSet row) {
		Enterprise enterprise = new Enterprise(id: row.getInt('id'), cnpj: row.getString('cnpj'))
		mapPersonFields(enterprise, row)
	}

	@Override
	protected int insertDetails(Connection connection, int personId, Enterprise enterprise) {
		SqlExecutor.insertReturningId(connection, INSERT_ENTERPRISE,
				[
					personId,
					DocumentNormalizer.normalizeCnpj(enterprise.cnpj)
				])
	}

	@Override
	protected void updateDetails(Connection connection, Enterprise enterprise) {
		SqlExecutor.update(connection, UPDATE_ENTERPRISE, [
			DocumentNormalizer.normalizeCnpj(enterprise.cnpj),
			enterprise.id
		])
	}

	@Override
	protected void loadRelations(Connection connection, Enterprise enterprise) {
		super.loadRelations(connection, enterprise)
		vacancyDAO.findByEnterprise(connection, enterprise.id).each { enterprise.addVacancy(it) }
	}
}
