package com.mariajuliasales.dao

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnection {

	private static final String DEFAULT_URL = 'jdbc:postgresql://localhost:5432/linketinder'
	private static final String DEFAULT_USER = 'postgres'
	private static final String DEFAULT_PASSWORD = 'postgres'

	final String url
	final String user
	final String password

	DatabaseConnection(
	String url = System.getenv('DB_URL') ?: DEFAULT_URL,
	String user = System.getenv('DB_USER') ?: DEFAULT_USER,
	String password = System.getenv('DB_PASSWORD') ?: DEFAULT_PASSWORD
	) {
		this.url = url
		this.user = user
		this.password = password
	}

	Connection getConnection() {
		DriverManager.getConnection(url, user, password)
	}

	def <T> T withConnection(Closure<T> work) {
		try {
			getConnection().withCloseable { Connection connection -> work(connection) }
		} catch (SQLException exception) {
			throw SqlExceptionTranslator.translate(exception)
		}
	}

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
