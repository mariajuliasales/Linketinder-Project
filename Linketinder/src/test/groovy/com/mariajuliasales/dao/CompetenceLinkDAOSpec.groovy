package com.mariajuliasales.dao

import com.mariajuliasales.model.Competence
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class CompetenceLinkDAOSpec extends Specification {

	Connection connection = Mock()
	PreparedStatement statement = Mock()
	ResultSet resultSet = Mock()
	CompetenceLinkDAO dao = new CompetenceLinkDAO(new CompetenceDAO(Stub(ConnectionManager)))

	def "findLinkedByOwners should load the competences of every owner in a single query"() {
		given:
		statement.executeQuery() >> resultSet
		resultSet.next() >>> [true, true, true, false]
		resultSet.getInt('owner_id') >>> [1, 1, 3]
		resultSet.getInt('id') >>> [10, 11, 12]
		resultSet.getString('name') >>> ['Java', 'SQL', 'Python']

		when:
		Map<Integer, List<Competence>> competences = dao.findLinkedByOwners(connection, CompetenceLink.CANDIDATE, [1, 2, 3])

		then:
		1 * connection.prepareStatement({ String sql ->
			sql.contains('FROM candidate_competence owner_link') &&
					sql.contains('owner_link.candidate_id IN (?, ?, ?)') &&
					sql.contains('ORDER BY owner_link.candidate_id, co.name')
		}) >> statement
		1 * statement.setObject(1, 1)
		1 * statement.setObject(2, 2)
		1 * statement.setObject(3, 3)
		competences.keySet() == [1, 3] as Set
		competences[1]*.name == ['Java', 'SQL']
		competences[1]*.id == [10, 11]
		competences[3]*.name == ['Python']
	}

	def "findLinkedByOwners should bind each repeated id only once"() {
		given:
		statement.executeQuery() >> resultSet
		resultSet.next() >> false

		when:
		dao.findLinkedByOwners(connection, CompetenceLink.ENTERPRISE, [5, 5, 7])

		then:
		1 * connection.prepareStatement({ String sql ->
			sql.contains('FROM enterprise_competence owner_link') && sql.contains('owner_link.enterprise_id IN (?, ?)')
		}) >> statement
		1 * statement.setObject(1, 5)
		1 * statement.setObject(2, 7)
		0 * statement.setObject(3, _)
	}

	def "findLinkedByOwners should not query the database without owners"() {
		when:
		Map<Integer, List<Competence>> competences = dao.findLinkedByOwners(connection, CompetenceLink.CANDIDATE, ids)

		then:
		0 * connection._
		competences == [:]

		where:
		ids << [[], null]
	}
}
