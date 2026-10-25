package com.mariajuliasales.dao

import com.mariajuliasales.model.AbstractPerson
import com.mariajuliasales.model.Person
import com.mariajuliasales.model.PersonType

import java.sql.Connection
import java.sql.ResultSet

abstract class PersonDAO<T extends AbstractPerson> {

	protected static final String PERSON_COLUMNS = "p.name, p.email, p.description, ${AddressDAO.COLUMNS}"

	private static final String INSERT_PERSON = '''
            INSERT INTO person (type, name, email, description, password_hash, address_id)
            VALUES (?::person_type, ?, ?, ?, crypt(?, gen_salt('bf')), ?)
            RETURNING id'''
	private static final String UPDATE_PERSON = 'UPDATE person SET name = ?, email = ?, description = ?, address_id = ? WHERE id = ?'
	private static final String UPDATE_PASSWORD = "UPDATE person SET password_hash = crypt(?, gen_salt('bf')) WHERE id = ?"

	protected final DatabaseConnection database
	protected final CompetenceDAO competenceDAO

	PersonDAO(DatabaseConnection database, CompetenceDAO competenceDAO) {
		this.database = database
		this.competenceDAO = competenceDAO
	}

	protected abstract String getTableName()

	protected abstract PersonType getPersonType()

	protected abstract CompetenceLink getCompetenceLink()

	protected abstract String getSelectSql()

	protected abstract T mapRow(ResultSet row)

	protected abstract int insertDetails(Connection connection, int personId, T person)

	protected abstract void updateDetails(Connection connection, T person)

	T create(T person) {
		database.withTransaction { Connection connection ->
			int personId = insertPerson(connection, person)
			person.id = insertDetails(connection, personId, person)
			saveCompetences(connection, person)
			person
		}
	}

	List<T> findAll() {
		findWhere('', [])
	}

	T findById(int id) {
		List<T> found = findWhere("WHERE ${tableName}.id = ?", [id])
		found ? found.first() : null
	}

	T update(T person) {
		database.withTransaction { Connection connection ->
			Integer personId = findPersonId(connection, person.id)
			if (personId == null) {
				return null
			}
			updatePerson(connection, personId, person)
			updateDetails(connection, person)
			saveCompetences(connection, person)
			person
		}
	}

	boolean delete(int id) {
		database.withTransaction { Connection connection ->
			Integer personId = findPersonId(connection, id)
			personId != null && SqlExecutor.update(connection, 'DELETE FROM person WHERE id = ?', [personId]) > 0
		}
	}

	protected void loadRelations(Connection connection, T person) {
		person.competences = competenceDAO.findLinked(connection, competenceLink, person.id)
	}

	protected static <P extends AbstractPerson> P mapPersonFields(P person, ResultSet row) {
		person.name = row.getString('name')
		person.email = row.getString('email')
		person.description = row.getString('description')
		person.address = AddressDAO.mapRow(row)
		person
	}

	// protected, e não private: no Groovy, closures da classe-pai não enxergam métodos private quando o objeto é uma subclasse
	protected List<T> findWhere(String condition, List<?> parameters) {
		database.withConnection { Connection connection ->
			String sql = "${selectSql} ${condition} ORDER BY ${tableName}.id"
			List<T> people = SqlExecutor.query(connection, sql, parameters) { ResultSet row -> mapRow(row) }
			people.each { T person -> loadRelations(connection, person) }
			people
		}
	}

	protected int insertPerson(Connection connection, Person person) {
		int addressId = AddressDAO.insert(connection, person.address)
		SqlExecutor.insertReturningId(connection, INSERT_PERSON,
				[
					personType.name(),
					person.name,
					person.email,
					person.description,
					person.password,
					addressId
				])
	}

	protected static void updatePerson(Connection connection, int personId, Person person) {
		int addressId = AddressDAO.insert(connection, person.address)
		SqlExecutor.update(connection, UPDATE_PERSON, [
			person.name,
			person.email,
			person.description,
			addressId,
			personId
		])
		if (person.password) {
			SqlExecutor.update(connection, UPDATE_PASSWORD, [person.password, personId])
		}
	}

	protected Integer findPersonId(Connection connection, int id) {
		SqlExecutor.queryFirst(connection, "SELECT person_id FROM ${tableName} WHERE id = ?", [id]) { ResultSet row ->
			row.getInt('person_id')
		}
	}

	protected void saveCompetences(Connection connection, T person) {
		competenceDAO.replaceLinks(connection, competenceLink, person.id, person.competences)
	}
}
