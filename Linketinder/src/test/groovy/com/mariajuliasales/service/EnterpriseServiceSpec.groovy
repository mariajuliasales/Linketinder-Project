package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionFactory
import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.dao.DatabaseConnection
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import spock.lang.Specification

import java.sql.Connection

import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validRecruiter

class EnterpriseServiceSpec extends Specification {

	static final int ENTERPRISE = 3
	static final int R1 = 7
	static final int R2 = 8

	Connection connection = Stub()
	ConnectionManager connectionManager = Stub() {
		withTransaction(_) >> { Closure work -> work(connection) }
	}
	EnterpriseRepository enterpriseRepository = Mock()
	RecruiterRepository recruiterRepository = Mock()
	EnterpriseService enterpriseService = createService(connectionManager)

	private EnterpriseService createService(ConnectionManager manager) {
		new EnterpriseService(enterpriseRepository, new EnterpriseValidator(new PersonValidator()), recruiterRepository,
		manager)
	}

	def "create should save a valid enterprise"() {
		given:
		Enterprise enterprise = validEnterprise()

		when:
		Enterprise result = enterpriseService.create(enterprise)

		then:
		1 * enterpriseRepository.create(enterprise) >> enterprise
		result == enterprise
	}

	def "create should not save an invalid enterprise"() {
		when:
		enterpriseService.create(validEnterprise(cnpj: "invalid"))

		then:
		thrown(ValidationException)
		0 * enterpriseRepository.create(_)
	}

	def "create should require a password"() {
		when:
		enterpriseService.create(validEnterprise(password: null))

		then:
		thrown(ValidationException)
		0 * enterpriseRepository.create(_)
	}

	def "find all should return the enterprises from the repository"() {
		given:
		List<Enterprise> enterprises = [
			validEnterprise(id: 1),
			validEnterprise(id: 2, email: "rh@inovacaodigital.io")
		]
		enterpriseRepository.findAll() >> enterprises

		expect:
		enterpriseService.findAll() == enterprises
	}

	def "find by id should return the enterprise from the repository"() {
		given:
		Enterprise enterprise = validEnterprise(id: 10)
		enterpriseRepository.findById(10) >> enterprise

		expect:
		enterpriseService.findById(10) == enterprise
	}

	def "find by id should fail when the enterprise does not exist"() {
		given:
		enterpriseRepository.findById(99) >> null

		when:
		enterpriseService.findById(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 99 não existe"
	}

	def "update should accept an enterprise without a new password"() {
		given:
		Enterprise enterprise = validEnterprise(password: null)

		when:
		Enterprise result = enterpriseService.update(enterprise)

		then:
		1 * enterpriseRepository.update(enterprise) >> enterprise
		result == enterprise
	}

	def "update should not save an invalid enterprise"() {
		when:
		enterpriseService.update(validEnterprise(email: "invalid"))

		then:
		thrown(ValidationException)
		0 * enterpriseRepository.update(_)
	}

	def "update should fail when the enterprise does not exist"() {
		given:
		enterpriseRepository.update(_) >> null

		when:
		enterpriseService.update(validEnterprise(id: 99))

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 99 não existe"
	}

	def "delete of an enterprise without recruiters should remove only the enterprise, as before"() {
		when:
		enterpriseService.delete(ENTERPRISE)

		then:
		1 * recruiterRepository.findAllByEnterprise(ENTERPRISE) >> []
		0 * recruiterRepository.delete(*_)
		1 * enterpriseRepository.delete(connection, ENTERPRISE) >> true
		0 * enterpriseRepository.delete(_ as Integer)
	}

	def "delete should remove the recruiter persons first and then the enterprise, in the same transaction (CA53)"() {
		when:
		enterpriseService.delete(ENTERPRISE)

		then:
		1 * recruiterRepository.findAllByEnterprise(ENTERPRISE) >> [
			validRecruiter(id: R1, enterpriseId: ENTERPRISE),
			validRecruiter(id: R2, enterpriseId: ENTERPRISE, email: 'r2@exemplo.com')
		]

		then:
		1 * recruiterRepository.delete(connection, R1) >> true
		1 * recruiterRepository.delete(connection, R2) >> true

		then:
		1 * enterpriseRepository.delete(connection, ENTERPRISE) >> true
	}

	def "after the enterprise is deleted, the e-mail of its recruiter can be registered again (CA53)"() {
		given: 'um banco em memória: o e-mail de R1 fica em uso enquanto a person de R1 existir'
		Set<Integer> recruiterPersons = [R1] as Set
		recruiterRepository.findAllByEnterprise(ENTERPRISE) >> [validRecruiter(id: R1, enterpriseId: ENTERPRISE)]
		recruiterRepository.delete(connection, R1) >> { recruiterPersons.remove(R1) }
		recruiterRepository.isEmailInUse('r1@exemplo.com') >> { !recruiterPersons.isEmpty() }
		enterpriseRepository.delete(connection, ENTERPRISE) >> true

		when:
		enterpriseService.delete(ENTERPRISE)

		then:
		recruiterPersons.isEmpty()
		!recruiterRepository.isEmailInUse('r1@exemplo.com')
	}

	def "delete should roll back the recruiter deletions when deleting the enterprise fails"() {
		given:
		Connection realConnection = Mock()
		ConnectionFactory connectionFactory = Stub() {
			createConnection() >> realConnection
		}
		EnterpriseService transactionalService = createService(new DatabaseConnection(connectionFactory))
		recruiterRepository.findAllByEnterprise(ENTERPRISE) >> [validRecruiter(id: R1, enterpriseId: ENTERPRISE)]

		when:
		transactionalService.delete(ENTERPRISE)

		then:
		1 * recruiterRepository.delete(realConnection, R1) >> true
		1 * enterpriseRepository.delete(realConnection, ENTERPRISE) >> { throw new IllegalStateException('falha ao apagar') }
		thrown(IllegalStateException)
		0 * realConnection.commit()
		1 * realConnection.rollback()
	}

	def "delete should fail with 404 and roll back when the enterprise does not exist"() {
		given:
		Connection realConnection = Mock()
		ConnectionFactory connectionFactory = Stub() {
			createConnection() >> realConnection
		}
		EnterpriseService transactionalService = createService(new DatabaseConnection(connectionFactory))
		recruiterRepository.findAllByEnterprise(99) >> []
		enterpriseRepository.delete(realConnection, 99) >> false

		when:
		transactionalService.delete(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 99 não existe"
		0 * realConnection.commit()
		1 * realConnection.rollback()
	}
}
