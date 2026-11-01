package com.mariajuliasales.dao

import spock.lang.Specification
import spock.lang.Unroll

class ConnectionFactoriesSpec extends Specification {

	private static DatabaseConfig configFor(String type, String url = 'jdbc:postgresql://localhost:5432/linketinder') {
		new DatabaseConfig(type, url, 'postgres', 'postgres')
	}

	@Unroll
	def "type '#type' should create a PostgreSQL connection factory"() {
		expect:
		ConnectionFactories.forConfig(configFor(type)) instanceof PostgresConnectionFactory

		where:
		type << ['postgresql', 'PostgreSQL']
	}

	@Unroll
	def "unsupported type '#type' should be rejected listing the supported ones"() {
		when:
		ConnectionFactories.forConfig(configFor(type))

		then:
		IllegalArgumentException exception = thrown()
		exception.message.contains('Banco de dados não suportado')
		exception.message.contains('postgresql')

		where:
		type << ['mysql', '', null]
	}

	def "PostgreSQL factory should reject an url from another database"() {
		when:
		ConnectionFactories.forConfig(configFor('postgresql', 'jdbc:mysql://localhost:3306/linketinder'))

		then:
		IllegalArgumentException exception = thrown()
		exception.message.contains('jdbc:postgresql:')
	}
}
