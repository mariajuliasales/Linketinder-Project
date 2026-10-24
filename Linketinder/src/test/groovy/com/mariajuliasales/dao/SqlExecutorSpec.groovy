package com.mariajuliasales.dao

import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class SqlExecutorSpec extends Specification {

	Connection connection = Mock()
	PreparedStatement statement = Mock()
	ResultSet resultSet = Mock()

	def "update should bind the parameters in order and return the affected rows"() {
		when:
		int affectedRows = SqlExecutor.update(connection, 'UPDATE competence SET name = ? WHERE id = ?', ['Java', 7])

		then:
		1 * connection.prepareStatement('UPDATE competence SET name = ? WHERE id = ?') >> statement
		1 * statement.setObject(1, 'Java')
		1 * statement.setObject(2, 7)
		1 * statement.executeUpdate() >> 1
		1 * statement.close()
		affectedRows == 1
	}

	def "query should map every row and close the resources"() {
		given:
		connection.prepareStatement(_ as String) >> statement
		statement.executeQuery() >> resultSet
		resultSet.next() >>> [true, true, false]
		resultSet.getString('name') >>> ['Java', 'SQL']

		when:
		List<String> names = SqlExecutor.query(connection, 'SELECT name FROM competence', []) { ResultSet row ->
			row.getString('name')
		}

		then:
		names == ['Java', 'SQL']
		1 * resultSet.close()
		1 * statement.close()
	}

	def "query first should return null when there are no rows"() {
		given:
		connection.prepareStatement(_ as String) >> statement
		statement.executeQuery() >> resultSet
		resultSet.next() >> false

		expect:
		SqlExecutor.queryFirst(connection, 'SELECT id FROM vacancy WHERE id = ?', [99]) { ResultSet row -> row.getInt('id') } == null
	}

	def "insert returning id should return the generated id"() {
		given:
		connection.prepareStatement(_ as String) >> statement
		statement.executeQuery() >> resultSet
		resultSet.next() >>> [true, false]
		resultSet.getInt('id') >> 42

		expect:
		SqlExecutor.insertReturningId(connection, 'INSERT INTO competence (name) VALUES (?) RETURNING id', ['Kotlin']) == 42
	}

	def "null parameters should be bound as null"() {
		when:
		SqlExecutor.update(connection, 'UPDATE address SET street = ? WHERE id = ?', [null, 1])

		then:
		1 * connection.prepareStatement(_ as String) >> statement
		1 * statement.setObject(1, null)
		1 * statement.setObject(2, 1)
	}
}
