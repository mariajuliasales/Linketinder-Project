package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.request.EnterpriseRequest
import com.mariajuliasales.dto.response.EnterpriseAnonymousResponse
import com.mariajuliasales.dto.response.EnterpriseResponse
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validVacancy

class EnterpriseControllerSpec extends Specification {

	EnterpriseRepository enterpriseRepository = Mock()
	EnterpriseController controller = new EnterpriseController(
	new EnterpriseService(enterpriseRepository, new EnterpriseValidator(new PersonValidator())))

	private static EnterpriseRequest request(String name = 'TechSolutions Brasil') {
		new EnterpriseRequest(name, 'contato@techsolutions.com.br', '123456',
				new AddressRequest('01310-000', null, null, null, null, 'São Paulo', 'SP', 'Brasil'),
				'Desenvolvimento de software', ['Java'], '11.222.333/0001-81')
	}

	def "create should convert the request, save the enterprise and return the response"() {
		when:
		EnterpriseResponse response = controller.create(request())

		then:
		1 * enterpriseRepository.create({ Enterprise enterprise -> enterprise.cnpj == '11.222.333/0001-81' }) >>
		{ Enterprise enterprise -> enterprise.id = 3; enterprise }
		response.id() == 3
		response.name() == 'TechSolutions Brasil'
	}

	def "find all should return only anonymous profiles"() {
		given:
		enterpriseRepository.findAll() >> [validEnterprise()]

		expect:
		controller.findAll().every { it instanceof EnterpriseAnonymousResponse }
	}

	def "update should save the enterprise with the given id and return it with its vacancies"() {
		given:
		Enterprise saved = validEnterprise(id: 4, name: 'Tech Nova')
		saved.addVacancy(validVacancy(enterprise: null))

		when:
		EnterpriseResponse response = controller.update(4, request('Tech Nova'))

		then:
		1 * enterpriseRepository.update({ Enterprise enterprise -> enterprise.id == 4 && enterprise.name == 'Tech Nova' }) >>
		{ Enterprise enterprise -> enterprise }
		1 * enterpriseRepository.findById(4) >> saved
		response.name() == 'Tech Nova'
		response.vacancies().size() == 1
	}
}
