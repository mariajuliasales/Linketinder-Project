package com.mariajuliasales.service

import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.model.Competence
import spock.lang.Specification
import spock.lang.Unroll

class CompetenceServiceSpec extends Specification {

    CompetenceDAO competenceDAO = Mock(CompetenceDAO)
    CompetenceService competenceService = new CompetenceService(competenceDAO)

    def "create competence with a new name"() {
        given:
        competenceDAO.findByName("Ilustrador") >> null

        when:
        Competence result = competenceService.create("Ilustrador")

        then:
        1 * competenceDAO.create(new Competence("Ilustrador")) >> new Competence(12, "Ilustrador")
        result.id == 12
    }

    def "create competence that already exists should fail"() {
        given:
        competenceDAO.findByName("java") >> new Competence(2, "Java")

        when:
        competenceService.create("java")

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Competence already exists"
        0 * competenceDAO.create(_)
    }

    @Unroll
    def "create competence with invalid name '#name' should fail"() {
        when:
        competenceService.create(name)

        then:
        IllegalArgumentException ex = thrown()
        ex.message.startsWith("Competence name")
        0 * competenceDAO.create(_)

        where:
        name << [null, "", "   ", "x" * 51]
    }

    def "find all competences should return the list from the DAO"() {
        given:
        List<Competence> competences = [new Competence(1, "Java"), new Competence(2, "Python")]
        competenceDAO.findAll() >> competences

        expect:
        competenceService.findAll() == competences
    }

    def "find competence by id that does not exist should fail"() {
        given:
        competenceDAO.findById(99) >> null

        when:
        competenceService.findById(99)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Competence not found"
    }

    def "update competence with a new name"() {
        given:
        competenceDAO.findByName("Spring Boot") >> null

        when:
        Competence result = competenceService.update(3, "Spring Boot")

        then:
        1 * competenceDAO.update(new Competence(3, "Spring Boot")) >> new Competence(3, "Spring Boot")
        result.name == "Spring Boot"
    }

    def "update competence to a name used by another competence should fail"() {
        given:
        competenceDAO.findByName("Java") >> new Competence(2, "Java")

        when:
        competenceService.update(3, "Java")

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Competence already exists"
        0 * competenceDAO.update(_)
    }

    def "delete competence that does not exist should fail"() {
        given:
        competenceDAO.delete(99) >> false

        when:
        competenceService.delete(99)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Competence not found"
    }

    def "competences with the same name in different cases are equal"() {
        expect:
        new Competence("Java") == new Competence("java")
        [new Competence("Java"), new Competence("JAVA")].unique().size() == 1
    }
}
