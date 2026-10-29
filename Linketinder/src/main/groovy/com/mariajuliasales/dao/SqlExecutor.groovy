package com.mariajuliasales.dao

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

final class SqlExecutor {

	static int insertReturningId(Connection connection, String sql, List<?> parameters) {
		queryFirst(connection, sql, parameters) { ResultSet row -> row.getInt('id') }
	}

	static int update(Connection connection, String sql, List<?> parameters) {
		connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
			bind(statement, parameters)
			statement.executeUpdate()
		}
	}

	static <T> List<T> query(Connection connection, String sql, List<?> parameters, Closure<T> mapRow) {
		connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
			bind(statement, parameters)
			statement.executeQuery().withCloseable { ResultSet resultSet -> mapAll(resultSet, mapRow) }
		}
	}

	static <T> T queryFirst(Connection connection, String sql, List<?> parameters, Closure<T> mapRow) {
		List<T> rows = query(connection, sql, parameters, mapRow)
		rows ? rows.first() : null
	}

	private static void bind(PreparedStatement statement, List<?> parameters) {
		parameters.eachWithIndex { Object value, int index ->
			statement.setObject(index + 1, value)
		}
	}

	private static <T> List<T> mapAll(ResultSet resultSet, Closure<T> mapRow) {
		List<T> rows = []
		while (resultSet.next()) {
			rows << mapRow(resultSet)
		}
		rows
	}
}
