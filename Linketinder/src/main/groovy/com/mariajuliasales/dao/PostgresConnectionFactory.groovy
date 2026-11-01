package com.mariajuliasales.dao

import java.sql.Connection
import java.sql.DriverManager

class PostgresConnectionFactory implements ConnectionFactory {

	static final String URL_PREFIX = 'jdbc:postgresql:'

	private final DatabaseConfig config

	PostgresConnectionFactory(DatabaseConfig config) {
		if (!config.url?.startsWith(URL_PREFIX)) {
			throw new IllegalArgumentException("URL inválida para PostgreSQL: '${config.url}'. Ela deve começar com ${URL_PREFIX}")
		}
		this.config = config
	}

	@Override
	Connection createConnection() {
		DriverManager.getConnection(config.url, config.user, config.password)
	}
}
