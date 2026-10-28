package com.mariajuliasales.dao

class DatabaseConfig {

	private static final String DEFAULT_URL = 'jdbc:postgresql://localhost:5432/linketinder'
	private static final String DEFAULT_USER = 'postgres'
	private static final String DEFAULT_PASSWORD = 'postgres'

	final String url
	final String user
	final String password

	DatabaseConfig(String url, String user, String password) {
		this.url = url
		this.user = user
		this.password = password
	}

	static DatabaseConfig fromEnvironment(Map<String, String> environment = System.getenv()) {
		new DatabaseConfig(
				environment.DB_URL ?: DEFAULT_URL,
				environment.DB_USER ?: DEFAULT_USER,
				environment.DB_PASSWORD ?: DEFAULT_PASSWORD
				)
	}
}
