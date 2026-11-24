package com.mariajuliasales.dao

import com.mariajuliasales.model.Candidate
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/** Usa o CandidateDAO como subclasse concreta do PersonDAO, com JDBC mockado (sem banco). */
class PersonDAOSpec extends Specification {

	Connection connection = Mock()
	PreparedStatement personStatement = Mock()
	PreparedStatement competenceStatement = Mock()
	ResultSet personRows = Mock()
	ResultSet competenceRows = Mock()
	ConnectionManager connectionManager = Stub() {
		withConnection(_) >> { Closure work -> work.call(connection) }
	}
	CandidateDAO dao = new CandidateDAO(connectionManager, new CompetenceLinkDAO(new CompetenceDAO(Stub(ConnectionManager))))

	def setup() {
		personStatement.executeQuery() >> personRows
		competenceStatement.executeQuery() >> competenceRows
	}

	private static boolean isPersonQuery(String sql) {
		sql.contains('FROM candidate\n')
	}

	private static boolean isCompetenceQuery(String sql) {
		sql.contains('FROM candidate_competence owner_link')
	}

	def "findAll should load the competences of every person in a single query"() {
		given:
		personRows.next() >>> [true, true, true, false]
		personRows.getInt('id') >>> [1, 2, 3]
		competenceRows.next() >>> [true, true, true, false]
		competenceRows.getInt('owner_id') >>> [1, 1, 3]
		competenceRows.getInt('id') >>> [10, 11, 12]
		competenceRows.getString('name') >>> ['Java', 'SQL', 'Python']

		when:
		List<Candidate> candidates = dao.findAll()

		then:
		1 * connection.prepareStatement({ isPersonQuery(it) }) >> personStatement
		1 * connection.prepareStatement({ isCompetenceQuery(it) }) >> competenceStatement
		0 * connection.prepareStatement(_)
		candidates*.id == [1, 2, 3]
		candidates[0].competences*.name == ['Java', 'SQL']
		candidates[2].competences*.name == ['Python']
	}

	def "a person without competences should get an empty list"() {
		given:
		personRows.next() >>> [true, false]
		personRows.getInt('id') >> 2
		competenceRows.next() >> false

		when:
		List<Candidate> candidates = dao.findAll()

		then:
		1 * connection.prepareStatement({ isPersonQuery(it) }) >> personStatement
		1 * connection.prepareStatement({ isCompetenceQuery(it) }) >> competenceStatement
		candidates.size() == 1
		candidates[0].competences == []
	}

	def "an empty listing should not query competences"() {
		given:
		personRows.next() >> false

		when:
		List<Candidate> candidates = dao.findAll()

		then:
		1 * connection.prepareStatement({ isPersonQuery(it) }) >> personStatement
		0 * connection.prepareStatement({ isCompetenceQuery(it) })
		candidates == []
	}

	def "reading a single person by #method should still load its competences ordered by name"() {
		given:
		personRows.next() >>> [true, false]
		personRows.getInt('id') >> 1
		competenceRows.next() >>> [true, true, false]
		competenceRows.getInt('owner_id') >> 1
		competenceRows.getInt('id') >>> [10, 11]
		competenceRows.getString('name') >>> ['Java', 'SQL']

		when:
		Candidate candidate = read(dao)

		then:
		1 * connection.prepareStatement({ isPersonQuery(it) }) >> personStatement
		1 * connection.prepareStatement({ String sql ->
			isCompetenceQuery(sql) && sql.contains('ORDER BY owner_link.candidate_id, co.name')
		}) >> competenceStatement
		1 * competenceStatement.setObject(1, 1)
		candidate.id == 1
		candidate.competences*.name == ['Java', 'SQL']

		where:
		method        | read
		'findById'    | { CandidateDAO it -> it.findById(1) }
		'findByEmail' | { CandidateDAO it -> it.findByEmail('ana@email.com') }
		'findByCpf'   | { CandidateDAO it -> it.findByCpf('529.982.247-25') }
	}
}
