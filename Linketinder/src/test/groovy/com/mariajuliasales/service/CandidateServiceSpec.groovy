package com.mariajuliasales.service

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence
import com.mariajuliasales.repository.Database
import spock.lang.Specification
import spock.lang.Unroll

class CandidateServiceSpec extends Specification {

    Database database = Mock(Database)
    CandidateService candidateService = new CandidateService(database)

    def "create candidate with valid data"() {
        given:
        Candidate candidate = new Candidate(1, "João", "joao@email.com", "SP", "12345-678", "Desenvolvedor Java",
                [Competence.JAVASCRIPT], "529.982.247-25", 30)

        when:
        Candidate createdCandidate = candidateService.create(candidate)

        then:
        createdCandidate == candidate
        1 * database.createCandidate(candidate) >> candidate
    }

    def "create candidate with null value should fail"() {
        when:
        candidateService.create(null)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid candidate data"
        0 * database.createCandidate(_)
    }

    @Unroll
    def "create candidate with invalid cpf should fail"() {
        given:
        Candidate candidate = new Candidate(1, "João", "joao@email.com", "SP", "12345-678", "Desenvolvedor Java",
                [Competence.JAVASCRIPT], cpf, 30)

        when:
        candidateService.create(candidate)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid candidate cpf"
        0 * database.createCandidate(_)

        where:
        cpf << ["invalid", "12345678900", "123.456.789-00", "cpf_invalid", "000.000.000-00"]
    }

    @Unroll
    def "create candidate with invalid email should fail"() {
        given:
        Candidate candidate = new Candidate(1, "João", email, "SP", "12345-678", "Desenvolvedor Java",
                [Competence.JAVASCRIPT], "529.982.247-25", 30)

        when:
        candidateService.create(candidate)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid candidate email"
        0 * database.createCandidate(_)

        where:
        email << ["invalid", "joaoemail.com", "joao@.com", "joao@email", "joao@email.", "529.982.247-25"]

    }

    def "get all candidates should return list from database"() {
        given:
        Candidate candidate = new Candidate(1, "Maria", "maria@email.com", "RJ", "98765-432", "Desenvolvedor Python",
                [Competence.SQL, Competence.GROOVY], "987.654.321-00", 25)
        List<Candidate> expectedCandidates = [candidate]
        database.getCandidates() >> expectedCandidates

        when:
        List<Candidate> result = candidateService.getAllCandidates()

        then:
        result == expectedCandidates
    }

    def "get candidate by id should return candidate from database"() {
        given:
        Candidate candidate = new Candidate(10, "Maria", "maria@email.com", "RJ", "98765-432", "Desenvolvedor Python",
                [Competence.SQL, Competence.GROOVY], "987.654.321-00", 25)
        database.findCandidateById(10) >> candidate

        when:
        Candidate result = candidateService.getCandidateById(10)

        then:
        result == candidate
    }
}