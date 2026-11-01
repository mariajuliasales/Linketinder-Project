package com.mariajuliasales.dao

import com.mariajuliasales.exception.DuplicateEntityException
import spock.lang.Specification

import java.sql.Connection
import java.sql.SQLException

class DatabaseConnectionSpec extends Specification {

	Connection connection = Mock()
	ConnectionFactory connectionFactory = Stub() {
		createConnection() >> connection
	}
	DatabaseConnection database = new DatabaseConnection(connectionFactory)

	def "with connection should give the created connection to the work and close it"() {
		when:
		String result = database.withConnection { Connection received ->
			assert received.is(connection)
			'done'
		}

		then:
		result == 'done'
		1 * connection.close()
	}

	def "with transaction should commit when the work succeeds"() {
		when:
		database.withTransaction { 'saved' }

		then:
		1 * connection.setAutoCommit(false)
		1 * connection.commit()
		0 * connection.rollback()
		1 * connection.close()
	}

	def "with transaction should roll back and rethrow when the work fails"() {
		when:
		database.withTransaction { throw new IllegalStateException('falhou') }

		then:
		thrown(IllegalStateException)
		0 * connection.commit()
		1 * connection.rollback()
		1 * connection.close()
	}

	def "database errors should be translated into business exceptions"() {
		when:
		database.withConnection { throw new SQLException('duplicate key', '23505') }

		then:
		thrown(DuplicateEntityException)
	}
}
