package com.mariajuliasales.dao

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.PersonType
import com.mariajuliasales.util.DocumentNormalizer

import java.sql.Connection
import java.sql.ResultSet
import java.time.LocalDate

class CandidateDAO extends PersonDAO<Candidate> {

	private static final String SELECT_CANDIDATE = """
            SELECT candidate.id, candidate.cpf, candidate.birth_date, candidate.training, ${PERSON_COLUMNS}
            FROM candidate
            JOIN person p  ON p.id = candidate.person_id
            JOIN address a ON a.id = p.address_id"""
	private static final String INSERT_CANDIDATE =
	'INSERT INTO candidate (person_id, cpf, birth_date, training) VALUES (?, ?, ?, ?) RETURNING id'
	private static final String UPDATE_CANDIDATE = 'UPDATE candidate SET cpf = ?, birth_date = ?, training = ? WHERE id = ?'

	CandidateDAO(DatabaseConnection database, CompetenceDAO competenceDAO) {
		super(database, competenceDAO)
	}

	@Override
	protected String getTableName() {
		'candidate'
	}

	@Override
	protected PersonType getPersonType() {
		PersonType.CANDIDATE
	}

	@Override
	protected CompetenceLink getCompetenceLink() {
		CompetenceLink.CANDIDATE
	}

	@Override
	protected String getSelectSql() {
		SELECT_CANDIDATE
	}

	@Override
	protected Candidate mapRow(ResultSet row) {
		Candidate candidate = new Candidate(
				id: row.getInt('id'),
				cpf: row.getString('cpf'),
				birthDate: row.getObject('birth_date', LocalDate),
				training: row.getString('training')
				)
		mapPersonFields(candidate, row)
	}

	@Override
	protected int insertDetails(Connection connection, int personId, Candidate candidate) {
		SqlExecutor.insertReturningId(connection, INSERT_CANDIDATE,
				[
					personId,
					DocumentNormalizer.onlyDigits(candidate.cpf),
					candidate.birthDate,
					candidate.training
				])
	}

	@Override
	protected void updateDetails(Connection connection, Candidate candidate) {
		SqlExecutor.update(connection, UPDATE_CANDIDATE,
				[
					DocumentNormalizer.onlyDigits(candidate.cpf),
					candidate.birthDate,
					candidate.training,
					candidate.id
				])
	}
}
