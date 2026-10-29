package com.mariajuliasales.service

import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validEnterprise

class EnterpriseServiceSpec extends Specification {

	EnterpriseRepository enterpriseRepository = Mock()
	EnterpriseService enterpriseService = new EnterpriseService(enterpriseRepository, new EnterpriseValidator(new PersonValidator()))

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

	def "delete should remove the enterprise through the repository"() {
		when:
		enterpriseService.delete(1)

		then:
		1 * enterpriseRepository.delete(1) >> true
	}

	def "delete should fail when the enterprise does not exist"() {
		given:
		enterpriseRepository.delete(99) >> false

		when:
		enterpriseService.delete(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 99 não existe"
	}
}
