package com.mariajuliasales.dao

import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus

import java.sql.Connection
import java.sql.ResultSet

class VacancyDAO {

	private static final String SELECT_VACANCY = """
            SELECT v.id, v.title, v.description, v.status, ${AddressDAO.COLUMNS},
                   e.id AS enterprise_id, e.cnpj, p.name AS enterprise_name, p.email AS enterprise_email
            FROM vacancy v
            JOIN address a    ON a.id = v.address_id
            JOIN enterprise e ON e.id = v.enterprise_id
            JOIN person p     ON p.id = e.person_id"""
	private static final String INSERT_VACANCY = '''
            INSERT INTO vacancy (enterprise_id, title, description, status, address_id)
            VALUES (?, ?, ?, ?::vacancy_status, ?)
            RETURNING id'''
	private static final String UPDATE_VACANCY = '''
            UPDATE vacancy
            SET title = ?, description = ?, status = ?::vacancy_status, address_id = ?
            WHERE id = ?'''
	private static final String WHERE_ID = 'WHERE v.id = ?'
	private static final String WHERE_ENTERPRISE = 'WHERE v.enterprise_id = ?'

	private final DatabaseConnection database
	private final CompetenceDAO competenceDAO

	VacancyDAO(DatabaseConnection database, CompetenceDAO competenceDAO) {
		this.database = database
		this.competenceDAO = competenceDAO
	}

	Vacancy create(Vacancy vacancy) {
		database.withTransaction { Connection connection ->
			int addressId = AddressDAO.insert(connection, vacancy.address)
			vacancy.id = SqlExecutor.insertReturningId(connection, INSERT_VACANCY,
					[
						vacancy.enterprise.id,
						vacancy.title,
						vacancy.description,
						statusName(vacancy),
						addressId
					])
			saveCompetences(connection, vacancy)
			vacancy
		}
	}

	List<Vacancy> findAll() {
		database.withConnection { Connection connection -> findWhere(connection, '', []) }
	}

	Vacancy findById(int id) {
		database.withConnection { Connection connection ->
			List<Vacancy> found = findWhere(connection, WHERE_ID, [id])
			found ? found.first() : null
		}
	}

	List<Vacancy> findByEnterprise(int enterpriseId) {
		database.withConnection { Connection connection -> findByEnterprise(connection, enterpriseId) }
	}

	List<Vacancy> findByEnterprise(Connection connection, int enterpriseId) {
		findWhere(connection, WHERE_ENTERPRISE, [enterpriseId])
	}

	Vacancy update(Vacancy vacancy) {
		database.withTransaction { Connection connection ->
			if (!exists(connection, vacancy.id)) {
				return null
			}
			int addressId = AddressDAO.insert(connection, vacancy.address)
			SqlExecutor.update(connection, UPDATE_VACANCY,
					[
						vacancy.title,
						vacancy.description,
						statusName(vacancy),
						addressId,
						vacancy.id
					])
			saveCompetences(connection, vacancy)
			vacancy
		}
	}

	boolean delete(int id) {
		database.withConnection { Connection connection ->
			SqlExecutor.update(connection, 'DELETE FROM vacancy WHERE id = ?', [id]) > 0
		}
	}

	private static boolean exists(Connection connection, int id) {
		SqlExecutor.queryFirst(connection, 'SELECT id FROM vacancy WHERE id = ?', [id]) { ResultSet row ->
			row.getInt('id')
		} != null
	}

	private static String statusName(Vacancy vacancy) {
		(vacancy.status ?: VacancyStatus.OPEN).name()
	}

	private void saveCompetences(Connection connection, Vacancy vacancy) {
		competenceDAO.replaceLinks(connection, CompetenceLink.VACANCY, vacancy.id, vacancy.competences)
	}

	private List<Vacancy> findWhere(Connection connection, String condition, List<?> parameters) {
		Map<Integer, Enterprise> enterprisesById = [:]
		String sql = "${SELECT_VACANCY} ${condition} ORDER BY v.id"
		List<Vacancy> vacancies = SqlExecutor.query(connection, sql, parameters) { ResultSet row ->
			mapRow(row, enterprisesById)
		}
		vacancies.each { Vacancy vacancy ->
			vacancy.competences = competenceDAO.findLinked(connection, CompetenceLink.VACANCY, vacancy.id)
		}
		vacancies
	}

	private static Vacancy mapRow(ResultSet row, Map<Integer, Enterprise> enterprisesById) {
		Vacancy vacancy = new Vacancy(
				id: row.getInt('id'),
				title: row.getString('title'),
				description: row.getString('description'),
				status: VacancyStatus.valueOf(row.getString('status')),
				address: AddressDAO.mapRow(row)
				)
		enterpriseOf(row, enterprisesById).addVacancy(vacancy)
		vacancy
	}

	private static Enterprise enterpriseOf(ResultSet row, Map<Integer, Enterprise> enterprisesById) {
		enterprisesById.computeIfAbsent(row.getInt('enterprise_id')) { Integer id ->
			new Enterprise(
					id: id,
					name: row.getString('enterprise_name'),
					email: row.getString('enterprise_email'),
					cnpj: row.getString('cnpj')
					)
		}
	}
}
