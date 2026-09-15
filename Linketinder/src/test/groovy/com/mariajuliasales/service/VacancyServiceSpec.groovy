package com.mariajuliasales.service

import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.repository.Database
import spock.lang.Specification

class VacancyServiceSpec extends Specification {

    Database database = Mock(Database)
    VacancyService vacancyService = new VacancyService(database)

    def "create vacancy valid"() {
        given:
        Enterprise enterprise = new Enterprise(1, "TechSolutions Brasil", "contato@techsolutions.com.br", "SP", "01310000", "Empresa especializada em desenvolvimento de software sob medida", [Competence.JAVA, Competence.SPRING_FRAMEWORK], "11222333000181", "Brasil")

        Vacancy vacancy = new Vacancy(id, title, description, competences, enterprise)

        when:
        def result = vacancyService.create(vacancy)

        then:
        1 * database.createVacancy(vacancy) >> vacancy
        result == vacancy

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | [Competence.JAVASCRIPT]
        2  | "Vaga 2" | "Descrição da vaga 2" | [Competence.SQL, Competence.GROOVY]
        3  | "Vaga 3" | "Descrição da vaga 3" | [Competence.JAVA, Competence.PYTHON]
    }

    def "create vacancy with null enterprise should fail"() {
        given:
        Vacancy vacancy = new Vacancy(id, title, description, competences, null)

        when:
        def result = vacancyService.create(vacancy)

        then:
        def exception = thrown(IllegalArgumentException)
        result == null

        and:
        exception.message == "The job opening must be associated with a company."
        0 * database.createVacancy(_)

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | [Competence.JAVASCRIPT]
    }

    def "create vacancy with null title should fail"() {
        given:
        Enterprise enterprise = new Enterprise(1, "TechSolutions Brasil", "contato@techsolutions.com.br", "SP", "01310000", "Empresa especializada em desenvolvimento de software sob medida", [Competence.JAVA, Competence.SPRING_FRAMEWORK], "11222333000181", "Brasil")
        Vacancy vacancy = new Vacancy(id, title, description, competences, enterprise)

        when:
        def result = vacancyService.create(vacancy)

        then:
        def exception = thrown(IllegalArgumentException)
        result == null

        and:
        exception.message == "The job opening must have a title."
        0 * database.createVacancy(_)

        where:
        id | title  | description           | competences
        1  | null   | "Descrição da vaga 1" | [Competence.JAVASCRIPT]
        2  | "    " | "Descrição da vaga 2" | [Competence.SQL, Competence.GROOVY]
        3  | ""     | "Descrição da vaga 3" | [Competence.JAVA, Competence.PYTHON]
    }

    def "create vacancy with invalid competences should fail"() {
        given:
        Enterprise enterprise = new Enterprise(1, "TechSolutions Brasil", "contato@techsolutions.com.br", "SP", "01310000", "Empresa especializada em desenvolvimento de software sob medida", [Competence.JAVA, Competence.SPRING_FRAMEWORK], "11222333000181", "Brasil")
        Vacancy vacancy = new Vacancy(id, title, description, competences, enterprise)


        when:
        def result = vacancyService.create(vacancy)

        then:
        def exception = thrown(IllegalArgumentException)
        result == null

        and:
        exception.message == "The job opening must have at least one required skill."
        0 * database.createVacancy(_)

        where:
        id | title    | description           | competences
        1  | "Vaga 1" | "Descrição da vaga 1" | []
        2  | "Vaga 2" | "Descrição da vaga 2" | ["JAVA", "PYTHON"]
        3  | "Vaga 3" | "Descrição da vaga 3" | ["    "]

    }
}