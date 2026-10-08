package com.mariajuliasales.service

import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import spock.lang.Specification
import spock.lang.Unroll

class EnterpriseServiceSpec extends Specification {

    EnterpriseDAO enterpriseDAO = Mock(EnterpriseDAO)
    EnterpriseService enterpriseService = new EnterpriseService(enterpriseDAO)

    private static Enterprise validEnterprise(Map changes = [:]) {
        Map fields = [id: 1, name: "TechSolutions Brasil", email: "contato@techsolutions.com.br", password: "123456",
                      address: validAddress(),
                      description: "Empresa especializada em desenvolvimento de software sob medida",
                      competences: [new Competence("Java"), new Competence("Spring Framework")],
                      cnpj: "00.000.000/E08G-12"] + changes
        new Enterprise(fields)
    }

    private static Address validAddress(Map changes = [:]) {
        new Address([cep: "01310000", city: "São Paulo", state: "SP", country: "Brasil"] + changes)
    }

    def "create enterprise with valid data"() {
        given:
        Enterprise enterprise = validEnterprise()

        when:
        Enterprise result = enterpriseService.create(enterprise)

        then:
        result != null
        result.name == enterprise.name
        result.email == enterprise.email
        1 * enterpriseDAO.create(_ as Enterprise) >> { Enterprise e -> enterprise }
    }

    @Unroll
    def "create enterprise with invalid cnpj should fail"() {
        given:
        Enterprise enterprise = validEnterprise(cnpj: cnpj)

        when:
        enterpriseService.create(enterprise)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid enterprise cnpj"
        0 * enterpriseDAO.create(_)

        where:
        cnpj << ["00.000.000/E08G-99", "AA.AAA.AAA/AAAA-AA", "12.ABC.345/01D", "cnpj_invalido", ""]

    }

    @Unroll
    def "create enterprise with invalid email should fail"() {
        given:
        Enterprise enterprise = validEnterprise(email: email)

        when:
        enterpriseService.create(enterprise)
        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid enterprise email"
        0 * enterpriseDAO.create(_)

        where:
        email << ["invalid", "joaoemail.com", "joao@.com", "joao@email", "joao@email.", "529.982.247-25, 11222333000181"]

    }

    @Unroll
    def "create enterprise with invalid #field should fail"() {
        given:
        Enterprise enterprise = validEnterprise([(field): value])

        when:
        enterpriseService.create(enterprise)

        then:
        IllegalArgumentException ex = thrown()
        ex.message.startsWith(message)
        0 * enterpriseDAO.create(_)

        where:
        field      | value   || message
        "password" | "123"   || "Invalid enterprise password"
        "password" | null    || "Invalid enterprise password"
        "address"  | null    || "Invalid enterprise address"
        "name"     | " "     || "Invalid enterprise name"
    }

    @Unroll
    def "create enterprise with invalid address #field should fail"() {
        given:
        Enterprise enterprise = validEnterprise(address: validAddress([(field): value]))

        when:
        enterpriseService.create(enterprise)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == message
        0 * enterpriseDAO.create(_)

        where:
        field   | value || message
        "cep"   | "abc" || "Invalid enterprise cep"
        "state" | "S"   || "Invalid enterprise city or state"
        "city"  | " "   || "Invalid enterprise city or state"
    }

    def "get all enterprises should return list from database"() {
        given:
        Enterprise enterprise1 = validEnterprise()
        Enterprise enterprise2 = validEnterprise(id: 2, name: "Inovacao Digital LTDA", email: "rh@inovacaodigital.io")
        enterpriseDAO.findAll() >> [enterprise1, enterprise2]

        when:
        List<Enterprise> result = enterpriseService.getAllEnterprises()

        then:
        result == [enterprise1, enterprise2]
    }

    def "get enterprise by id should return enterprise from database"() {
        given:
        Enterprise enterprise = validEnterprise(id: 10)
        enterpriseDAO.findById(10) >> enterprise

        when:
        Enterprise result = enterpriseService.getEnterpriseById(10)

        then:
        result == enterprise
    }

    def "update enterprise with valid data"() {
        given:
        Enterprise enterprise = validEnterprise(password: null)

        when:
        Enterprise result = enterpriseService.update(enterprise)

        then:
        1 * enterpriseDAO.update(enterprise) >> enterprise
        result == enterprise
    }

    def "update enterprise that does not exist should fail"() {
        given:
        enterpriseDAO.update(_) >> null

        when:
        enterpriseService.update(validEnterprise(id: 99))

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Enterprise not found"
    }

    def "delete enterprise should call the DAO"() {
        when:
        enterpriseService.delete(1)

        then:
        1 * enterpriseDAO.delete(1) >> true
    }

    def "delete enterprise that does not exist should fail"() {
        given:
        enterpriseDAO.delete(99) >> false

        when:
        enterpriseService.delete(99)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Enterprise not found"
    }
}