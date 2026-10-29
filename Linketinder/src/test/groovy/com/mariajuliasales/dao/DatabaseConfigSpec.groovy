package com.mariajuliasales.dao

import spock.lang.Specification

class DatabaseConfigSpec extends Specification {

	def "configuration should use the values from the environment"() {
		when:
		DatabaseConfig config = DatabaseConfig.fromEnvironment(
				DB_URL: 'jdbc:postgresql://db:5432/test', DB_USER: 'admin', DB_PASSWORD: 'secret')

		then:
		config.url == 'jdbc:postgresql://db:5432/test'
		config.user == 'admin'
		config.password == 'secret'
	}

	def "configuration should fall back to the local defaults"() {
		when:
		DatabaseConfig config = DatabaseConfig.fromEnvironment([:])

		then:
		config.url == 'jdbc:postgresql://localhost:5432/linketinder'
		config.user == 'postgres'
		config.password == 'postgres'
	}
}
