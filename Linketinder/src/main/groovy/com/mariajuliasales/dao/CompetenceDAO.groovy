package com.mariajuliasales.dao

import com.mariajuliasales.model.Competence

import java.sql.Connection
import java.sql.ResultSet

class CompetenceDAO {

	private static final String SELECT_COMPETENCE = 'SELECT id, name FROM competence'

	private final DatabaseConnection database

	CompetenceDAO(DatabaseConnection database) {
		this.database = database
	}

	Competence create(Competence competence) {
		database.withConnection { Connection connection -> insert(connection, competence.name) }
	}

	List<Competence> findAll() {
		database.withConnection { Connection connection ->
			SqlExecutor.query(connection, "${SELECT_COMPETENCE} ORDER BY name", []) { ResultSet row -> mapRow(row) }
		}
	}

	Competence findById(int id) {
		database.withConnection { Connection connection ->
			SqlExecutor.queryFirst(connection, "${SELECT_COMPETENCE} WHERE id = ?", [id]) { ResultSet row -> mapRow(row) }
		}
	}

	Competence findByName(String name) {
		database.withConnection { Connection connection -> selectByName(connection, name) }
	}

	Competence update(Competence competence) {
		database.withConnection { Connection connection ->
			int updatedRows = SqlExecutor.update(connection, 'UPDATE competence SET name = ? WHERE id = ?',
					[
						competence.name,
						competence.id
					])
			updatedRows > 0 ? competence : null
		}
	}

	boolean delete(int id) {
		database.withConnection { Connection connection ->
			SqlExecutor.update(connection, 'DELETE FROM competence WHERE id = ?', [id]) > 0
		}
	}

	void replaceLinks(Connection connection, CompetenceLink link, int ownerId, List<Competence> competences) {
		SqlExecutor.update(connection, "DELETE FROM ${link.table} WHERE ${link.ownerColumn} = ?", [ownerId])

		String insertLink = "INSERT INTO ${link.table} (${link.ownerColumn}, competence_id) VALUES (?, ?) " +
				'ON CONFLICT DO NOTHING'
		competences.findAll { it?.name }.unique().each { Competence competence ->
			competence.id = findOrCreate(connection, competence.name).id
			SqlExecutor.update(connection, insertLink, [ownerId, competence.id])
		}
	}

	List<Competence> findLinked(Connection connection, CompetenceLink link, int ownerId) {
		String sql = """
                SELECT co.id, co.name
                FROM ${link.table} owner_link
                JOIN competence co ON co.id = owner_link.competence_id
                WHERE owner_link.${link.ownerColumn} = ?
                ORDER BY co.name"""
		SqlExecutor.query(connection, sql, [ownerId]) { ResultSet row -> mapRow(row) }
	}

	private static Competence findOrCreate(Connection connection, String name) {
		selectByName(connection, name) ?: insert(connection, name)
	}

	private static Competence insert(Connection connection, String name) {
		SqlExecutor.queryFirst(connection, 'INSERT INTO competence (name) VALUES (?) RETURNING id, name',
				[name.trim()]) { ResultSet row -> mapRow(row) }
	}

	private static Competence selectByName(Connection connection, String name) {
		SqlExecutor.queryFirst(connection, "${SELECT_COMPETENCE} WHERE lower(name) = lower(?)",
				[name?.trim()]) { ResultSet row -> mapRow(row) }
	}

	private static Competence mapRow(ResultSet row) {
		new Competence(row.getInt('id'), row.getString('name'))
	}
}
