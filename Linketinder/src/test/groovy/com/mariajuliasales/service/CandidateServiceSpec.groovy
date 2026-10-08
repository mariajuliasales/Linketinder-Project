package com.mariajuliasales.service

import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

class CandidateServiceSpec extends Specification {

    CandidateDAO candidateDAO = Mock(CandidateDAO)
    CandidateService candidateService = new CandidateService(candidateDAO)

    private static Candidate validCandidate(Map changes = [:]) {
        Map fields = [id: 1, name: "João", email: "joao@email.com", password: "123456",
                      address: validAddress(), description: "Desenvolvedor Java",
                      competences: [new Competence("JavaScript")], cpf: "529.982.247-25",
                      birthDate: LocalDate.of(1995, 5, 20), training: "Ciência da Computação"] + changes
        new Candidate(fields)
    }

    private static Address validAddress(Map changes = [:]) {
        new Address([cep: "12345-678", street: "Rua das Flores", number: "100", city: "São Paulo", state: "SP"] + changes)
    }

    def "create candidate with valid data"() {
        given:
        Candidate candidate = validCandidate()

        when:
        Candidate createdCandidate = candidateService.create(candidate)

        then:
        createdCandidate == candidate
        1 * candidateDAO.create(candidate) >> candidate
    }

    def "create candidate with null value should fail"() {
        when:
        candidateService.create(null)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid candidate data"
        0 * candidateDAO.create(_)
    }

    @Unroll
    def "create candidate with invalid cpf should fail"() {
        given:
        Candidate candidate = validCandidate(cpf: cpf)

        when:
        candidateService.create(candidate)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid candidate cpf"
        0 * candidateDAO.create(_)

        where:
        cpf << ["invalid", "12345678900", "123.456.789-00", "cpf_invalid", "000.000.000-00"]
    }

    @Unroll
    def "create candidate with invalid email should fail"() {
        given:
        Candidate candidate = validCandidate(email: email)

        when:
        candidateService.create(candidate)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid candidate email"
        0 * candidateDAO.create(_)

        where:
        email << ["invalid", "joaoemail.com", "joao@.com", "joao@email", "joao@email.", "529.982.247-25"]

    }

    @Unroll
    def "create candidate with invalid password should fail"() {
        given:
        Candidate candidate = validCandidate(password: password)

        when:
        candidateService.create(candidate)

        then:
        IllegalArgumentException ex = thrown()
        ex.message.startsWith("Invalid candidate password")
        0 * candidateDAO.create(_)

        where:
        password << [null, "", "12345"]
    }

    @Unroll
    def "create candidate with invalid #field should fail"() {
        given:
        Candidate candidate = validCandidate([(field): value])

        when:
        candidateService.create(candidate)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == message
        0 * candidateDAO.create(_)

        where:
        field       | value     || message
        "address"   | null      || "Invalid candidate address"
        "birthDate" | null      || "Invalid candidate birth date"
        "training"  | " "       || "Invalid candidate training"
        "name"      | ""        || "Invalid candidate name"
    }

    @Unroll
    def "create candidate with invalid address #field should fail"() {
        given:
        Candidate candidate = validCandidate(address: validAddress([(field): value]))

        when:
        candidateService.create(candidate)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == message
        0 * candidateDAO.create(_)

        where:
        field   | value || message
        "cep"   | "123" || "Invalid candidate cep"
        "cep"   | null  || "Invalid candidate cep"
        "state" | "São" || "Invalid candidate city or state"
        "city"  | ""    || "Invalid candidate city or state"
    }

    def "get all candidates should return list from database"() {
        given:
        Candidate candidate = validCandidate(name: "Maria")
        List<Candidate> expectedCandidates = [candidate]
        candidateDAO.findAll() >> expectedCandidates

        when:
        List<Candidate> result = candidateService.getAllCandidates()

        then:
        result == expectedCandidates
    }

    def "get candidate by id should return candidate from database"() {
        given:
        Candidate candidate = validCandidate(id: 10)
        candidateDAO.findById(10) >> candidate

        when:
        Candidate result = candidateService.getCandidateById(10)

        then:
        result == candidate
    }

    def "update candidate without new password should keep the current one"() {
        given:
        Candidate candidate = validCandidate(password: "")

        when:
        Candidate result = candidateService.update(candidate)

        then:
        1 * candidateDAO.update(candidate) >> candidate
        result == candidate
    }

    def "update candidate that does not exist should fail"() {
        given:
        candidateDAO.update(_) >> null

        when:
        candidateService.update(validCandidate(id: 99))

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Candidate not found"
    }

    def "delete candidate should call the DAO"() {
        when:
        candidateService.delete(1)

        then:
        1 * candidateDAO.delete(1) >> true
    }

    def "delete candidate that does not exist should fail"() {
        given:
        candidateDAO.delete(99) >> false

        when:
        candidateService.delete(99)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Candidate not found"
    }
}