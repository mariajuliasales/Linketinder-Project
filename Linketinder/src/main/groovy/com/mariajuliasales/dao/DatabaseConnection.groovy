package com.mariajuliasales.dao

import java.sql.Connection
import java.sql.SQLException

class DatabaseConnection implements ConnectionManager {

	private final ConnectionFactory connectionFactory

	DatabaseConnection(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory
	}

	@Override
	def <T> T withConnection(Closure<T> work) {
		try {
			connectionFactory.createConnection().withCloseable { Connection connection -> work(connection) }
		} catch (SQLException exception) {
			throw SqlExceptionTranslator.translate(exception)
		}
	}

	@Override
	def <T> T withTransaction(Closure<T> work) {
		withConnection { Connection connection ->
			connection.autoCommit = false
			try {
				T result = work(connection)
				connection.commit()
				result
			} catch (Exception exception) {
				rollback(connection, exception)
				throw exception
			}
		}
	}

	private static void rollback(Connection connection, Exception cause) {
		try {
			connection.rollback()
		} catch (SQLException rollbackFailure) {
			cause.addSuppressed(rollbackFailure)
		}
	}
}
