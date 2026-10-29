package com.mariajuliasales.dao

import com.mariajuliasales.model.Competence
import com.mariajuliasales.repository.CompetenceRepository

import java.sql.Connection
import java.sql.ResultSet

class CompetenceDAO implements CompetenceRepository {

	private static final String SELECT_COMPETENCE = 'SELECT id, name FROM competence'

	private final ConnectionManager connectionManager

	CompetenceDAO(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager
	}

	@Override
	Competence create(Competence competence) {
		connectionManager.withConnection { Connection connection -> insert(connection, competence.name) }
	}

	@Override
	List<Competence> findAll() {
		connectionManager.withConnection { Connection connection ->
			SqlExecutor.query(connection, "${SELECT_COMPETENCE} ORDER BY name", []) { ResultSet row -> mapRow(row) }
		}
	}

	@Override
	Competence findById(int id) {
		connectionManager.withConnection { Connection connection ->
			SqlExecutor.queryFirst(connection, "${SELECT_COMPETENCE} WHERE id = ?", [id]) { ResultSet row -> mapRow(row) }
		}
	}

	@Override
	Competence findByName(String name) {
		connectionManager.withConnection { Connection connection -> selectByName(connection, name) }
	}

	@Override
	Competence update(Competence competence) {
		connectionManager.withConnection { Connection connection ->
			int updatedRows = SqlExecutor.update(connection, 'UPDATE competence SET name = ? WHERE id = ?',
					[
						competence.name,
						competence.id
					])
			updatedRows > 0 ? competence : null
		}
	}

	@Override
	boolean delete(int id) {
		connectionManager.withConnection { Connection connection ->
			SqlExecutor.update(connection, 'DELETE FROM competence WHERE id = ?', [id]) > 0
		}
	}

	Competence findOrCreate(Connection connection, String name) {
		selectByName(connection, name) ?: insert(connection, name)
	}

	static Competence mapRow(ResultSet row) {
		new Competence(row.getInt('id'), row.getString('name'))
	}

	private static Competence insert(Connection connection, String name) {
		SqlExecutor.queryFirst(connection, 'INSERT INTO competence (name) VALUES (?) RETURNING id, name',
				[name.trim()]) { ResultSet row -> mapRow(row) }
	}

	private static Competence selectByName(Connection connection, String name) {
		SqlExecutor.queryFirst(connection, "${SELECT_COMPETENCE} WHERE lower(name) = lower(?)",
				[name?.trim()]) { ResultSet row -> mapRow(row) }
	}
}
