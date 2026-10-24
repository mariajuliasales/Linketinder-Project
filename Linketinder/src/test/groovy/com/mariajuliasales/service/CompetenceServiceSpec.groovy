package com.mariajuliasales.service

import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Competence
import spock.lang.Specification

class CompetenceServiceSpec extends Specification {

	CompetenceDAO competenceDAO = Mock(CompetenceDAO)
	CompetenceService competenceService = new CompetenceService(competenceDAO)

	def "create should save a competence with a new name"() {
		given:
		competenceDAO.findByName("Ilustrador") >> null

		when:
		Competence result = competenceService.create("Ilustrador")

		then:
		1 * competenceDAO.create(new Competence("Ilustrador")) >> new Competence(12, "Ilustrador")
		result.id == 12
	}

	def "create should fail when the name already exists"() {
		given:
		competenceDAO.findByName("java") >> new Competence(2, "Java")

		when:
		competenceService.create("java")

		then:
		DuplicateEntityException exception = thrown()
		exception.message == "Competência 'java' já cadastrada"
		0 * competenceDAO.create(_)
	}

	def "create should not save an invalid name"() {
		when:
		competenceService.create("   ")

		then:
		thrown(ValidationException)
		0 * competenceDAO.create(_)
	}

	def "find all should return the competences from the DAO"() {
		given:
		List<Competence> competences = [
			new Competence(1, "Java"),
			new Competence(2, "Python")
		]
		competenceDAO.findAll() >> competences

		expect:
		competenceService.findAll() == competences
	}

	def "find by id should return the competence from the DAO"() {
		given:
		Competence competence = new Competence(3, "SQL")
		competenceDAO.findById(3) >> competence

		expect:
		competenceService.findById(3) == competence
	}

	def "find by id should fail when the competence does not exist"() {
		given:
		competenceDAO.findById(99) >> null

		when:
		competenceService.findById(99)

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Competência com ID 99 não existe"
	}

	def "update should rename the competence"() {
		given:
		competenceDAO.findByName("Spring Boot") >> null

		when:
		Competence result = competenceService.update(3, "Spring Boot")

		then:
		1 * competenceDAO.update(new Competence(3, "Spring Boot")) >> new Competence(3, "Spring Boot")
		result.name == "Spring Boot"
	}

	def "update should accept the current name of the same competence"() {
		given:
		competenceDAO.findByName("java") >> new Competence(3, "Java")

		when:
		competenceService.update(3, "java")

		then:
		1 * competenceDAO.update(new Competence(3, "java")) >> new Competence(3, "java")
	}

	def "update should fail when another competence has the name"() {
		given:
		competenceDAO.findByName("Java") >> new Competence(2, "Java")

		when:
		competenceService.update(3, "Java")

		then:
		thrown(DuplicateEntityException)
		0 * competenceDAO.update(_)
	}

	def "update should fail when the competence does not exist"() {
		given:
		competenceDAO.findByName("Kotlin") >> null
		competenceDAO.update(_) >> null

		when:
		competenceService.update(99, "Kotlin")

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Competência com ID 99 não existe"
	}

	def "delete should remove the competence through the DAO"() {
		when:
		competenceService.delete(1)

		then:
		1 * competenceDAO.delete(1) >> true
	}

	def "delete should fail when the competence does not exist"() {
		given:
		competenceDAO.delete(99) >> false

		when:
		competenceService.delete(99)

		then:
		thrown(EntityNotFoundException)
	}
}
