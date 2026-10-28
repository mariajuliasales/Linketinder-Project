package com.mariajuliasales.service

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Competence
import com.mariajuliasales.repository.CompetenceRepository
import com.mariajuliasales.service.validator.CompetenceValidator
import spock.lang.Specification

class CompetenceServiceSpec extends Specification {

	CompetenceRepository competenceRepository = Mock()
	CompetenceService competenceService = new CompetenceService(competenceRepository, new CompetenceValidator())

	def "create should save a competence with a new name"() {
		given:
		competenceRepository.findByName("Ilustrador") >> null

		when:
		Competence result = competenceService.create(new Competence("Ilustrador"))

		then:
		1 * competenceRepository.create(new Competence("Ilustrador")) >> new Competence(12, "Ilustrador")
		result.id == 12
	}

	def "create should fail when the name already exists"() {
		given:
		competenceRepository.findByName("java") >> new Competence(2, "Java")

		when:
		competenceService.create(new Competence("java"))

		then:
		DuplicateEntityException exception = thrown()
		exception.message == "Competência 'java' já cadastrada"
		0 * competenceRepository.create(_)
	}

	def "create should not save an invalid name"() {
		when:
		competenceService.create(new Competence("   "))

		then:
		thrown(ValidationException)
		0 * competenceRepository.create(_)
	}

	def "find all should return the competences from the repository"() {
		given:
		List<Competence> competences = [
			new Competence(1, "Java"),
			new Competence(2, "Python")
		]
		competenceRepository.findAll() >> competences

		expect:
		competenceService.findAll() == competences
	}

	def "find by id should return the competence from the repository"() {
		given:
		Competence competence = new Competence(3, "SQL")
		competenceRepository.findById(3) >> competence

		expect:
		competenceService.findById(3) == competence
	}

	def "find by id should fail when the competence does not exist"() {
		given:
		competenceRepository.findById(99) >> null

		when:
		competenceService.findById(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Competência com ID 99 não existe"
	}

	def "update should rename the competence"() {
		given:
		competenceRepository.findByName("Spring Boot") >> null

		when:
		Competence result = competenceService.update(new Competence(3, "Spring Boot"))

		then:
		1 * competenceRepository.update(new Competence(3, "Spring Boot")) >> new Competence(3, "Spring Boot")
		result.name == "Spring Boot"
	}

	def "update should accept the current name of the same competence"() {
		given:
		competenceRepository.findByName("java") >> new Competence(3, "Java")

		when:
		competenceService.update(new Competence(3, "java"))

		then:
		1 * competenceRepository.update(new Competence(3, "java")) >> new Competence(3, "java")
	}

	def "update should fail when another competence has the name"() {
		given:
		competenceRepository.findByName("Java") >> new Competence(2, "Java")

		when:
		competenceService.update(new Competence(3, "Java"))

		then:
		thrown(DuplicateEntityException)
		0 * competenceRepository.update(_)
	}

	def "update should fail when the competence does not exist"() {
		given:
		competenceRepository.findByName("Kotlin") >> null
		competenceRepository.update(_) >> null

		when:
		competenceService.update(new Competence(99, "Kotlin"))

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Competência com ID 99 não existe"
	}

	def "delete should remove the competence through the repository"() {
		when:
		competenceService.delete(1)

		then:
		1 * competenceRepository.delete(1) >> true
	}

	def "delete should fail when the competence does not exist"() {
		given:
		competenceRepository.delete(99) >> false

		when:
		competenceService.delete(99)

		then:
		thrown(EntityNotFoundException)
	}
}
