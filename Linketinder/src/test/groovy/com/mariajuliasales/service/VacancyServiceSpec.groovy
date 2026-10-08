package com.mariajuliasales.service

import com.mariajuliasales.dao.VacancyDAO
import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import spock.lang.Specification

class VacancyServiceSpec extends Specification {

    VacancyDAO vacancyDAO = Mock(VacancyDAO)
    VacancyService vacancyService = new VacancyService(vacancyDAO)
    Enterprise enterprise = new Enterprise(id: 1, name: "TechSolutions Brasil", email: "contato@techsolutions.com.br", cnpj: "11222333000181")

    private Vacancy newVacancy(int id, String title, String description, List competences, Enterprise owner = enterprise) {
        new Vacancy(id: id, title: title, description: description, competences: competences,
                address: new Address(city: "São Paulo", state: "SP"), enterprise: owner)
    }

    def "create vacancy valid"() {
        given:
        Vacancy vacancy = newVacancy(id, title, description, competences)

        when:
        def result = vacancyService.create(vacancy)

        then:
        1 * vacancyDAO.create(vacancy) >> vacancy
        result == vacancy

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | [new Competence("JavaScript")]
        2  | "Vaga 2" | "Descrição da vaga 2" | [new Competence("SQL"), new Competence("Groovy")]
        3  | "Vaga 3" | "Descrição da vaga 3" | [new Competence("Java"), new Competence("Ilustrador")]
    }

    def "create vacancy should add it to the enterprise vacancies"() {
        given:
        Vacancy vacancy = newVacancy(1, "Vaga 1", "Descrição", [new Competence("Java")])
        vacancyDAO.create(vacancy) >> vacancy

        when:
        vacancyService.create(vacancy)

        then:
        enterprise.vacancies == [vacancy]
        vacancy.enterprise.is(enterprise)
    }

    def "create vacancy with null enterprise should fail"() {
        given:
        Vacancy vacancy = newVacancy(id, title, description, competences, null)

        when:
        def result = vacancyService.create(vacancy)

        then:
        def exception = thrown(IllegalArgumentException)
        result == null

        and:
        exception.message == "The job opening must be associated with a company."
        0 * vacancyDAO.create(_)

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | [new Competence("JavaScript")]
    }

    def "create vacancy with null title should fail"() {
        given:
        Vacancy vacancy = newVacancy(id, title, description, competences)

        when:
        def result = vacancyService.create(vacancy)

        then:
        def exception = thrown(IllegalArgumentException)
        result == null

        and:
        exception.message == "The job opening must have a title."
        0 * vacancyDAO.create(_)

        where:
        id | title  | description           | competences
        1  | null   | "Descrição da vaga 1" | [new Competence("JavaScript")]
        2  | "    " | "Descrição da vaga 2" | [new Competence("SQL"), new Competence("Groovy")]
        3  | ""     | "Descrição da vaga 3" | [new Competence("Java"), new Competence("Python")]
    }

    def "create vacancy with invalid competences should fail"() {
        given:
        Vacancy vacancy = newVacancy(id, title, description, competences)


        when:
        def result = vacancyService.create(vacancy)

        then:
        def exception = thrown(IllegalArgumentException)
        result == null

        and:
        exception.message == "The job opening must have at least one required skill."
        0 * vacancyDAO.create(_)

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | []
        2  | "Vaga 2" | "Descrição da vaga 2" | ["JAVA", "PYTHON"]
        3  | "Vaga 3" | "Descrição da vaga 3" | ["    "]

    }

    def "create vacancy without description or valid location should fail"() {
        given:
        Vacancy vacancy = newVacancy(1, "Vaga 1", description, [new Competence("Java")])
        vacancy.address = city == null ? null : new Address(city: city, state: state)

        when:
        vacancyService.create(vacancy)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == message
        0 * vacancyDAO.create(_)

        where:
        description | city        | state  || message
        ""          | "São Paulo" | "SP"   || "The job opening must have a description."
        "Descrição" | ""          | "SP"   || "The job opening must have a valid city and state."
        "Descrição" | "São Paulo" | "SPX"  || "The job opening must have a valid city and state."
        "Descrição" | null        | null   || "The job opening must have a valid city and state."
    }

    def "get all vacancies should return list of vacancies"() {
        given:
        Vacancy vacancy = newVacancy(id, title, description, competences)

        when:
        vacancyService.create(vacancy)
        def result = vacancyService.findAll()

        then:
        1 * vacancyDAO.findAll() >> [vacancy]
        result == [vacancy]

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | [new Competence("JavaScript")]
        2  | "Vaga 2" | "Descrição da vaga 2" | [new Competence("SQL"), new Competence("Groovy")]
        3  | "Vaga 3" | "Descrição da vaga 3" | [new Competence("Java"), new Competence("Python")]

    }

    def "get all vacancies should return empty list when no vacancies exist"() {
        when:
        def result = vacancyService.findAll()

        then:
        1 * vacancyDAO.findAll() >> []

        and:
        result != null
        result.isEmpty()

    }

    def "get vacancy by id should return the correct vacancy"() {
        given:
        Vacancy vacancy = newVacancy(id, title, description, competences)

        when:
        def result = vacancyService.findById(vacancy.id)

        then:
        1 * vacancyDAO.findById(vacancy.id) >> vacancy
        result == vacancy

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | [new Competence("JavaScript")]
        2  | "Vaga 2" | "Descrição da vaga 2" | [new Competence("SQL"), new Competence("Groovy")]
        3  | "Vaga 3" | "Descrição da vaga 3" | [new Competence("Java"), new Competence("Python")]

    }

    def "get vacancy by id should throw exception for invalid id"() {
        when:
        vacancyService.findById(invalidId)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == expectedMessage
        0 * vacancyDAO.findById(_)

        where:
        invalidId | expectedMessage
        -1        | "The job opening ID must be a positive integer."
        0         | "The job opening ID must be a positive integer."
    }

    def "get vacancy by id should throw exception when vacancy not found"() {
        given:
        int nonExistentId = 999

        when:
        vacancyService.findById(nonExistentId)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "No job opening found with the provided ID."
        1 * vacancyDAO.findById(nonExistentId) >> null
    }

    def "find vacancies by enterprise should return the enterprise vacancies"() {
        given:
        List<Vacancy> vacancies = [newVacancy(1, "Vaga 1", "Descrição", [new Competence("Java")])]

        when:
        def result = vacancyService.findByEnterprise(enterprise.id)

        then:
        1 * vacancyDAO.findByEnterprise(enterprise.id) >> vacancies
        result == vacancies
    }

    def "update vacancy with valid data"() {
        given:
        Vacancy vacancy = newVacancy(1, "Vaga 1", "Descrição", [new Competence("Java")])

        when:
        def result = vacancyService.update(vacancy)

        then:
        1 * vacancyDAO.update(vacancy) >> vacancy
        result == vacancy
    }

    def "update vacancy that does not exist should fail"() {
        given:
        vacancyDAO.update(_) >> null

        when:
        vacancyService.update(newVacancy(99, "Vaga", "Descrição", [new Competence("Java")]))

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "No job opening found with the provided ID."
    }

    def "delete vacancy should call the DAO"() {
        when:
        vacancyService.delete(1)

        then:
        1 * vacancyDAO.delete(1) >> true
    }

    def "delete vacancy that does not exist should fail"() {
        given:
        vacancyDAO.delete(99) >> false

        when:
        vacancyService.delete(99)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "No job opening found with the provided ID."
    }

}