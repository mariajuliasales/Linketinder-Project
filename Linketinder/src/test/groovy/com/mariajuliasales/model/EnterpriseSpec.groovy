package com.mariajuliasales.model

import spock.lang.Specification

class EnterpriseSpec extends Specification {

    Enterprise enterprise = new Enterprise(id: 1, name: "TechSolutions Brasil")
    Vacancy vacancy = new Vacancy(id: 10, title: "Desenvolvedor Groovy")

    def "add vacancy should link both sides of the relationship"() {
        when:
        enterprise.addVacancy(vacancy)

        then:
        enterprise.vacancies == [vacancy]
        vacancy.enterprise.is(enterprise)
    }

    def "add the same vacancy twice should not duplicate it"() {
        when:
        enterprise.addVacancy(vacancy)
        enterprise.addVacancy(vacancy)

        then:
        enterprise.vacancies.size() == 1
    }

    def "add null vacancy should be ignored"() {
        when:
        enterprise.addVacancy(null)

        then:
        enterprise.vacancies.isEmpty()
    }

    def "add vacancy from another enterprise should move it"() {
        given:
        Enterprise other = new Enterprise(id: 2, name: "Inovacao Digital LTDA")
        other.addVacancy(vacancy)

        when:
        enterprise.addVacancy(vacancy)

        then:
        other.vacancies.isEmpty()
        enterprise.vacancies == [vacancy]
        vacancy.enterprise.is(enterprise)
    }

    def "remove vacancy should unlink both sides of the relationship"() {
        given:
        enterprise.addVacancy(vacancy)

        when:
        enterprise.removeVacancy(vacancy)

        then:
        enterprise.vacancies.isEmpty()
        vacancy.enterprise == null
    }

    def "remove vacancy that is not in the list should not change it"() {
        given:
        Enterprise other = new Enterprise(id: 2, name: "Inovacao Digital LTDA")
        other.addVacancy(vacancy)

        when:
        enterprise.removeVacancy(vacancy)

        then:
        vacancy.enterprise.is(other)
        other.vacancies == [vacancy]
    }

    def "anonymous profile should show the number of vacancies"() {
        given:
        enterprise.addVacancy(vacancy)

        expect:
        enterprise.viewProfileAnonymous().endsWith("Vagas: 1")
    }
}
