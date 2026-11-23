package com.mariajuliasales.dao

import com.mariajuliasales.model.PersonType
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.model.RecruitmentArea
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.util.DocumentNormalizer

import java.sql.Connection
import java.sql.ResultSet
import java.time.LocalDate

/**
 * Não estende o PersonDAO: o template dele grava endereço e competências, e o recrutador não tem nenhum dos dois.
 * As consultas nunca selecionam password_hash.
 */
class RecruiterDAO implements RecruiterRepository {

	private static final String SELECT_RECRUITER = '''
            SELECT recruiter.id, recruiter.enterprise_id, recruiter.cpf, recruiter.birth_date,
                   ra.name AS area_name, p.name, p.email, p.description
            FROM recruiter
            JOIN person p            ON p.id = recruiter.person_id
            JOIN recruitment_area ra ON ra.id = recruiter.area_id'''
	private static final String INSERT_PERSON = '''
            INSERT INTO person (type, name, email, description, password_hash, address_id)
            VALUES (?::person_type, ?, ?, ?, crypt(?, gen_salt('bf')), NULL)
            RETURNING id'''
	private static final String INSERT_RECRUITER = '''
            INSERT INTO recruiter (person_id, enterprise_id, area_id, cpf, birth_date)
            VALUES (?, ?, (SELECT id FROM recruitment_area WHERE name = ?), ?, ?)
            RETURNING id'''
	private static final String EMAIL_IN_USE = 'SELECT EXISTS (SELECT 1 FROM person WHERE lower(email) = lower(?)) AS in_use'
	private static final String FIND_PERSON_ID = 'SELECT person_id AS id FROM recruiter WHERE id = ?'
	private static final String UPDATE_DESCRIPTION = 'UPDATE person SET description = ? WHERE id = ?'
	private static final String UPDATE_PASSWORD = "UPDATE person SET password_hash = crypt(?, gen_salt('bf')) WHERE id = ?"
	private static final String DELETE_PERSON = 'DELETE FROM person WHERE id = (SELECT person_id FROM recruiter WHERE id = ?)'

	private final ConnectionManager connectionManager

	RecruiterDAO(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager
	}

	@Override
	Recruiter create(Recruiter recruiter) {
		connectionManager.withTransaction { Connection connection ->
			int personId = SqlExecutor.insertReturningId(connection, INSERT_PERSON,
					[
						PersonType.RECRUITER.name(),
						recruiter.name,
						recruiter.email,
						recruiter.description,
						recruiter.password
					])
			recruiter.id = SqlExecutor.insertReturningId(connection, INSERT_RECRUITER,
					[
						personId,
						recruiter.enterpriseId,
						recruiter.area?.displayName,
						DocumentNormalizer.onlyDigits(recruiter.cpf),
						recruiter.birthDate
					])
			recruiter
		}
	}

	@Override
	Recruiter findById(int id) {
		findFirstWhere('WHERE recruiter.id = ?', [id])
	}

	@Override
	Recruiter findByIdAndEnterprise(int id, int enterpriseId) {
		findFirstWhere('WHERE recruiter.id = ? AND recruiter.enterprise_id = ?', [id, enterpriseId])
	}

	@Override
	List<Recruiter> findAllByEnterprise(int enterpriseId) {
		findWhere('WHERE recruiter.enterprise_id = ?', [enterpriseId])
	}

	@Override
	boolean isEmailInUse(String email) {
		connectionManager.withConnection { Connection connection ->
			SqlExecutor.queryFirst(connection, EMAIL_IN_USE, [email?.trim()]) { ResultSet row -> row.getBoolean('in_use') }
		}
	}

	@Override
	boolean updateProfile(int recruiterId, String description, String password) {
		connectionManager.withTransaction { Connection connection ->
			Integer personId = SqlExecutor.queryFirst(connection, FIND_PERSON_ID, [recruiterId]) { ResultSet row -> row.getInt('id') }
			if (personId == null) {
				return false
			}
			SqlExecutor.update(connection, UPDATE_DESCRIPTION, [description, personId])
			if (password) {
				SqlExecutor.update(connection, UPDATE_PASSWORD, [password, personId])
			}
			true
		}
	}

	@Override
	boolean delete(Connection connection, int id) {
		SqlExecutor.update(connection, DELETE_PERSON, [id]) > 0
	}

	private List<Recruiter> findWhere(String condition, List<?> parameters) {
		connectionManager.withConnection { Connection connection ->
			String sql = "${SELECT_RECRUITER} ${condition} ORDER BY recruiter.id"
			SqlExecutor.query(connection, sql, parameters) { ResultSet row -> mapRow(row) }
		}
	}

	private Recruiter findFirstWhere(String condition, List<?> parameters) {
		List<Recruiter> found = findWhere(condition, parameters)
		found ? found.first() : null
	}

	private static Recruiter mapRow(ResultSet row) {
		new Recruiter(
				id: row.getInt('id'),
				enterpriseId: row.getInt('enterprise_id'),
				area: RecruitmentArea.fromName(row.getString('area_name')),
				cpf: row.getString('cpf'),
				birthDate: row.getObject('birth_date', LocalDate),
				name: row.getString('name'),
				email: row.getString('email'),
				description: row.getString('description')
				)
	}
}
