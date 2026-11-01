package com.mariajuliasales.controller

import com.mariajuliasales.dto.response.CompetenceResponse
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.model.Competence
import com.mariajuliasales.repository.CompetenceRepository
import com.mariajuliasales.service.CompetenceService
import com.mariajuliasales.service.validator.CompetenceValidator
import spock.lang.Specification

class CompetenceControllerSpec extends Specification {

	CompetenceRepository competenceRepository = Mock()
	CompetenceController controller = new CompetenceController(
	new CompetenceService(competenceRepository, new CompetenceValidator()))

	def "find all should return id and name of each competence"() {
		given:
		competenceRepository.findAll() >> [new Competence(1, 'Java'), new Competence(2, 'SQL')]

		expect:
		controller.findAll() == [new CompetenceResponse(1, 'Java'), new CompetenceResponse(2, 'SQL')]
	}

	def "create should save the typed name"() {
		when:
		CompetenceResponse response = controller.create(' Kotlin ')

		then:
		1 * competenceRepository.create({ Competence competence -> competence.name == 'Kotlin' }) >>
		{ Competence competence -> new Competence(9, competence.name) }
		response == new CompetenceResponse(9, 'Kotlin')
	}

	def "update should reject a name already used by another competence"() {
		given:
		competenceRepository.findByName('Java') >> new Competence(1, 'Java')

		when:
		controller.update(2, 'Java')

		then:
		thrown(DuplicateEntityException)
		0 * competenceRepository.update(_)
	}
}
