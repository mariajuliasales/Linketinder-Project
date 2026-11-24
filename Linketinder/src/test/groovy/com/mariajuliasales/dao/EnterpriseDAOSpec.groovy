package com.mariajuliasales.dao

import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import spock.lang.Specification

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/** Vagas das empresas carregadas em lote pelo gancho loadRelations, com JDBC mockado (sem banco). */
class EnterpriseDAOSpec extends Specification {

	Connection connection = Mock()
	PreparedStatement enterpriseStatement = Mock()
	PreparedStatement competenceStatement = Mock()
	PreparedStatement vacancyStatement = Mock()
	PreparedStatement vacancyCompetenceStatement = Mock()
	ResultSet enterpriseRows = Mock()
	ResultSet competenceRows = Mock()
	ResultSet vacancyRows = Mock()
	ResultSet vacancyCompetenceRows = Mock()
	ConnectionManager connectionManager = Stub() {
		withConnection(_) >> { Closure work -> work.call(connection) }
	}
	CompetenceLinkDAO competenceLinkDAO = new CompetenceLinkDAO(new CompetenceDAO(Stub(ConnectionManager)))
	EnterpriseDAO dao = new EnterpriseDAO(connectionManager, competenceLinkDAO,
	new VacancyDAO(connectionManager, competenceLinkDAO))

	def setup() {
		enterpriseStatement.executeQuery() >> enterpriseRows
		competenceStatement.executeQuery() >> competenceRows
		vacancyStatement.executeQuery() >> vacancyRows
		vacancyCompetenceStatement.executeQuery() >> vacancyCompetenceRows
		competenceRows.next() >> false
		vacancyCompetenceRows.next() >> false
		vacancyRows.getString('status') >> 'OPEN'
	}

	private static boolean isEnterpriseQuery(String sql) {
		sql.contains('FROM enterprise\n')
	}

	private static boolean isEnterpriseCompetenceQuery(String sql) {
		sql.contains('FROM enterprise_competence owner_link')
	}

	private static boolean isVacancyQuery(String sql) {
		sql.contains('FROM vacancy v')
	}

	// As competências de cada vaga ainda são lidas uma vez por vaga (fica para a T43)
	private static boolean isVacancyCompetenceQuery(String sql) {
		sql.contains('FROM vacancy_competence owner_link')
	}

	private static boolean ownsAll(Enterprise enterprise) {
		enterprise.vacancies.every { Vacancy vacancy -> vacancy.enterprise.is(enterprise) }
	}

	def "findAll should load the vacancies of every enterprise in a single query"() {
		given:
		enterpriseRows.next() >>> [true, true, true, false]
		enterpriseRows.getInt('id') >>> [1, 2, 3]
		vacancyRows.next() >>> [true, true, true, false]
		vacancyRows.getInt('id') >>> [10, 11, 12]
		vacancyRows.getInt('enterprise_id') >>> [1, 3, 1]

		when:
		List<Enterprise> enterprises = dao.findAll()

		then:
		1 * connection.prepareStatement({ isEnterpriseQuery(it) }) >> enterpriseStatement
		1 * connection.prepareStatement({ isEnterpriseCompetenceQuery(it) }) >> competenceStatement
		1 * connection.prepareStatement({ String sql ->
			isVacancyQuery(sql) && sql.contains('WHERE v.enterprise_id IN (?, ?, ?)') && sql.contains('ORDER BY v.id')
		}) >> vacancyStatement
		1 * vacancyStatement.setObject(1, 1)
		1 * vacancyStatement.setObject(2, 2)
		1 * vacancyStatement.setObject(3, 3)
		3 * connection.prepareStatement({ isVacancyCompetenceQuery(it) }) >> vacancyCompetenceStatement
		0 * connection.prepareStatement(_)
		enterprises*.id == [1, 2, 3]
		enterprises[0].vacancies*.id == [10, 12]
		enterprises[2].vacancies*.id == [11]
		enterprises.every { ownsAll(it) }
	}

	def "an enterprise without vacancies should get an empty list"() {
		given:
		enterpriseRows.next() >>> [true, false]
		enterpriseRows.getInt('id') >> 2
		vacancyRows.next() >> false

		when:
		List<Enterprise> enterprises = dao.findAll()

		then:
		1 * connection.prepareStatement({ isEnterpriseQuery(it) }) >> enterpriseStatement
		1 * connection.prepareStatement({ isEnterpriseCompetenceQuery(it) }) >> competenceStatement
		1 * connection.prepareStatement({ isVacancyQuery(it) }) >> vacancyStatement
		enterprises.size() == 1
		enterprises[0].vacancies == []
	}

	def "an empty listing should not query vacancies"() {
		given:
		enterpriseRows.next() >> false

		when:
		List<Enterprise> enterprises = dao.findAll()

		then:
		1 * connection.prepareStatement({ isEnterpriseQuery(it) }) >> enterpriseStatement
		0 * connection.prepareStatement({ isVacancyQuery(it) })
		0 * connection.prepareStatement({ isEnterpriseCompetenceQuery(it) })
		enterprises == []
	}

	def "reading a single enterprise by #method should still load its vacancies pointing to the same instance"() {
		given:
		enterpriseRows.next() >>> [true, false]
		enterpriseRows.getInt('id') >> 1
		vacancyRows.next() >>> [true, true, false]
		vacancyRows.getInt('id') >>> [10, 11]
		vacancyRows.getInt('enterprise_id') >> 1

		when:
		Enterprise enterprise = read(dao)

		then:
		1 * connection.prepareStatement({ isEnterpriseQuery(it) }) >> enterpriseStatement
		1 * connection.prepareStatement({ isEnterpriseCompetenceQuery(it) }) >> competenceStatement
		1 * connection.prepareStatement({ String sql ->
			isVacancyQuery(sql) && sql.contains('WHERE v.enterprise_id IN (?)')
		}) >> vacancyStatement
		1 * vacancyStatement.setObject(1, 1)
		2 * connection.prepareStatement({ isVacancyCompetenceQuery(it) }) >> vacancyCompetenceStatement
		enterprise.id == 1
		enterprise.vacancies*.id == [10, 11]
		enterprise.vacancies[0].enterprise.is(enterprise)
		enterprise.vacancies[1].enterprise.is(enterprise)

		where:
		method       | read
		'findById'   | { EnterpriseDAO it -> it.findById(1) }
		'findByCnpj' | { EnterpriseDAO it -> it.findByCnpj('11.222.333/0001-81') }
	}
}
