package com.mariajuliasales.dao

import com.mariajuliasales.model.Vacancy
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/** Competências das vagas carregadas numa única consulta por listagem, com JDBC mockado (sem banco). */
class VacancyDAOSpec extends Specification {

	Connection connection = Mock()
	PreparedStatement vacancyStatement = Mock()
	PreparedStatement competenceStatement = Mock()
	ResultSet vacancyRows = Mock()
	ResultSet competenceRows = Mock()
	ConnectionManager connectionManager = Stub() {
		withConnection(_) >> { Closure work -> work.call(connection) }
	}
	VacancyDAO dao = new VacancyDAO(connectionManager, new CompetenceLinkDAO(new CompetenceDAO(Stub(ConnectionManager))))

	def setup() {
		vacancyStatement.executeQuery() >> vacancyRows
		competenceStatement.executeQuery() >> competenceRows
		vacancyRows.getString('status') >> 'OPEN'
	}

	private static boolean isVacancyQuery(String sql) {
		sql.contains('FROM vacancy v')
	}

	private static boolean isVacancyCompetenceQuery(String sql) {
		sql.contains('FROM vacancy_competence owner_link')
	}

	def "findAll should load the competences of every vacancy in a single query"() {
		given:
		vacancyRows.next() >>> [true, true, true, false]
		vacancyRows.getInt('id') >>> [10, 11, 12]
		vacancyRows.getInt('enterprise_id') >>> [1, 2, 1]
		competenceRows.next() >>> [true, true, true, false]
		competenceRows.getInt('owner_id') >>> [10, 10, 12]
		competenceRows.getInt('id') >>> [3, 1, 2]
		competenceRows.getString('name') >>> ['Docker', 'Java', 'SQL']

		when:
		List<Vacancy> vacancies = dao.findAll()

		then:
		1 * connection.prepareStatement({ isVacancyQuery(it) }) >> vacancyStatement
		1 * connection.prepareStatement({ String sql ->
			isVacancyCompetenceQuery(sql) &&
					sql.contains('owner_link.vacancy_id IN (?, ?, ?)') &&
					sql.contains('ORDER BY owner_link.vacancy_id, co.name')
		}) >> competenceStatement
		1 * competenceStatement.setObject(1, 10)
		1 * competenceStatement.setObject(2, 11)
		1 * competenceStatement.setObject(3, 12)
		0 * connection.prepareStatement(_)
		vacancies*.id == [10, 11, 12]
		vacancies[0].competences*.name == ['Docker', 'Java']
		vacancies[0].competences*.id == [3, 1]
		vacancies[2].competences*.name == ['SQL']
	}

	def "a vacancy without competences should get an empty list"() {
		given:
		vacancyRows.next() >>> [true, false]
		vacancyRows.getInt('id') >> 10
		vacancyRows.getInt('enterprise_id') >> 1
		competenceRows.next() >> false

		when:
		List<Vacancy> vacancies = dao.findAll()

		then:
		1 * connection.prepareStatement({ isVacancyQuery(it) }) >> vacancyStatement
		1 * connection.prepareStatement({ isVacancyCompetenceQuery(it) }) >> competenceStatement
		vacancies.size() == 1
		vacancies[0].competences == []
	}

	def "an empty listing should not query competences"() {
		given:
		vacancyRows.next() >> false

		when:
		List<Vacancy> vacancies = dao.findAll()

		then:
		1 * connection.prepareStatement({ isVacancyQuery(it) }) >> vacancyStatement
		0 * connection.prepareStatement({ isVacancyCompetenceQuery(it) })
		vacancies == []
	}

	def "findById should still load the competences ordered by name"() {
		given:
		vacancyRows.next() >>> [true, false]
		vacancyRows.getInt('id') >> 10
		vacancyRows.getInt('enterprise_id') >> 1
		competenceRows.next() >>> [true, true, false]
		competenceRows.getInt('owner_id') >> 10
		competenceRows.getInt('id') >>> [3, 1]
		competenceRows.getString('name') >>> ['Docker', 'Java']

		when:
		Vacancy vacancy = dao.findById(10)

		then:
		1 * connection.prepareStatement({ String sql -> isVacancyQuery(sql) && sql.contains('WHERE v.id = ?') }) >> vacancyStatement
		1 * connection.prepareStatement({ String sql ->
			isVacancyCompetenceQuery(sql) && sql.contains('owner_link.vacancy_id IN (?)')
		}) >> competenceStatement
		0 * connection.prepareStatement(_)
		vacancy.id == 10
		vacancy.competences*.name == ['Docker', 'Java']
	}

	def "#method should still load the competences ordered by name in a single query"() {
		given:
		vacancyRows.next() >>> [true, true, false]
		vacancyRows.getInt('id') >>> [10, 11]
		vacancyRows.getInt('enterprise_id') >> 1
		competenceRows.next() >>> [true, true, true, false]
		competenceRows.getInt('owner_id') >>> [10, 10, 11]
		competenceRows.getInt('id') >>> [3, 1, 2]
		competenceRows.getString('name') >>> ['Docker', 'Java', 'SQL']

		when:
		List<Vacancy> vacancies = read(dao, connection)

		then:
		1 * connection.prepareStatement({ String sql ->
			isVacancyQuery(sql) && sql.contains('WHERE v.enterprise_id = ?')
		}) >> vacancyStatement
		1 * connection.prepareStatement({ String sql ->
			isVacancyCompetenceQuery(sql) && sql.contains('owner_link.vacancy_id IN (?, ?)')
		}) >> competenceStatement
		0 * connection.prepareStatement(_)
		vacancies*.id == [10, 11]
		vacancies[0].competences*.name == ['Docker', 'Java']
		vacancies[1].competences*.name == ['SQL']

		where:
		method                              | read
		'findByEnterprise(int)'             | { VacancyDAO it, Connection c -> it.findByEnterprise(1) }
		'findByEnterprise(Connection, int)' | { VacancyDAO it, Connection c -> it.findByEnterprise(c, 1) }
	}

	def "findByEnterprises should load the competences of all enterprises in a single query"() {
		given:
		vacancyRows.next() >>> [true, true, true, false]
		vacancyRows.getInt('id') >>> [10, 11, 12]
		vacancyRows.getInt('enterprise_id') >>> [1, 2, 1]
		competenceRows.next() >>> [true, true, false]
		competenceRows.getInt('owner_id') >>> [11, 12]
		competenceRows.getInt('id') >>> [1, 2]
		competenceRows.getString('name') >>> ['Java', 'SQL']

		when:
		Map<Integer, List<Vacancy>> vacanciesByEnterprise = dao.findByEnterprises(connection, [1, 2])

		then:
		1 * connection.prepareStatement({ isVacancyQuery(it) }) >> vacancyStatement
		1 * connection.prepareStatement({ String sql ->
			isVacancyCompetenceQuery(sql) && sql.contains('owner_link.vacancy_id IN (?, ?, ?)')
		}) >> competenceStatement
		0 * connection.prepareStatement(_)
		vacanciesByEnterprise[1]*.id == [10, 12]
		vacanciesByEnterprise[1][0].competences == []
		vacanciesByEnterprise[1][1].competences*.name == ['SQL']
		vacanciesByEnterprise[2][0].competences*.name == ['Java']
	}
}
