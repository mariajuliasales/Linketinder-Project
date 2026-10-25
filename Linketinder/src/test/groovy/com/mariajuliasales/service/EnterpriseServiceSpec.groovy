package com.mariajuliasales.service

import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Enterprise
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validEnterprise

class EnterpriseServiceSpec extends Specification {

	EnterpriseDAO enterpriseDAO = Mock(EnterpriseDAO)
	EnterpriseService enterpriseService = new EnterpriseService(enterpriseDAO)

	def "create should save a valid enterprise"() {
		given:
		Enterprise enterprise = validEnterprise()

		when:
		Enterprise result = enterpriseService.create(enterprise)

		then:
		1 * enterpriseDAO.create(enterprise) >> enterprise
		result == enterprise
	}

	def "create should not save an invalid enterprise"() {
		when:
		enterpriseService.create(validEnterprise(cnpj: "invalid"))

		then:
		thrown(ValidationException)
		0 * enterpriseDAO.create(_)
	}

	def "create should require a password"() {
		when:
		enterpriseService.create(validEnterprise(password: null))

		then:
		thrown(ValidationException)
		0 * enterpriseDAO.create(_)
	}

	def "find all should return the enterprises from the DAO"() {
		given:
		List<Enterprise> enterprises = [
			validEnterprise(id: 1),
			validEnterprise(id: 2, email: "rh@inovacaodigital.io")
		]
		enterpriseDAO.findAll() >> enterprises

		expect:
		enterpriseService.findAll() == enterprises
	}

	def "find by id should return the enterprise from the DAO"() {
		given:
		Enterprise enterprise = validEnterprise(id: 10)
		enterpriseDAO.findById(10) >> enterprise

		expect:
		enterpriseService.findById(10) == enterprise
	}

	def "find by id should fail when the enterprise does not exist"() {
		given:
		enterpriseDAO.findById(99) >> null

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
		1 * enterpriseDAO.update(enterprise) >> enterprise
		result == enterprise
	}

	def "update should not save an invalid enterprise"() {
		when:
		enterpriseService.update(validEnterprise(email: "invalid"))

		then:
		thrown(ValidationException)
		0 * enterpriseDAO.update(_)
	}

	def "update should fail when the enterprise does not exist"() {
		given:
		enterpriseDAO.update(_) >> null

		when:
		enterpriseService.update(validEnterprise(id: 99))

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 99 não existe"
	}

	def "delete should remove the enterprise through the DAO"() {
		when:
		enterpriseService.delete(1)

		then:
		1 * enterpriseDAO.delete(1) >> true
	}

	def "delete should fail when the enterprise does not exist"() {
		given:
		enterpriseDAO.delete(99) >> false

		when:
		enterpriseService.delete(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 99 não existe"
	}
}
