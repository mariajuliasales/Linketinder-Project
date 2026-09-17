package com.mariajuliasales.service

import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.repository.Database
import spock.lang.Specification
import spock.lang.Unroll

class EnterpriseServiceSpec extends Specification {

    Database database = Mock(Database)
    EnterpriseService enterpriseService = new EnterpriseService(database)

    def "create enterprise with valid data"() {
        given:
        Enterprise enterprise = new Enterprise(1, "TechSolutions Brasil", "contato@techsolutions.com.br", "SP", "01310000", "Empresa especializada em desenvolvimento de software sob medida",
                [Competence.JAVA, Competence.SPRING_FRAMEWORK], "00.000.000/E08G-12", "Brasil"
        )

        when:
        Enterprise result = enterpriseService.create(enterprise)

        then:
        result != null
        result.name == enterprise.name
        result.email == enterprise.email
        1 * database.createEnterprise(_ as Enterprise) >> { Enterprise e -> enterprise }
    }

    @Unroll
    def "create enterprise with invalid cnpj should fail"() {
        given:
        Enterprise enterprise = new Enterprise(1, "TechSolutions Brasil", "contato@techsolutions.com.br", "SP", "01310000", "Empresa especializada em desenvolvimento de software sob medida",
                [Competence.JAVA, Competence.SPRING_FRAMEWORK], cnpj, "Brasil"
        )

        when:
        enterpriseService.create(enterprise)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid enterprise cnpj"
        0 * database.createEnterprise(_)

        where:
        cnpj << ["00.000.000/E08G-99", "AA.AAA.AAA/AAAA-AA", "12.ABC.345/01D", "cnpj_invalido", ""]

    }

    @Unroll
    def "create enterprise with invalid email should fail"() {
        given:
        Enterprise enterprise = new Enterprise(1, "TechSolutions Brasil", email, "SP", "01310000", "Empresa especializada em desenvolvimento de software sob medida",
                [Competence.JAVA, Competence.SPRING_FRAMEWORK], "00.000.000/E08G-12", "Brasil"
        )

        when:
        enterpriseService.create(enterprise)
        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Invalid enterprise email"
        0 * database.createEnterprise(_)

        where:
        email << ["invalid", "joaoemail.com", "joao@.com", "joao@email", "joao@email.", "529.982.247-25, 11222333000181"]

    }

    def "get all enterprises should return list from database"() {
        given:
        Enterprise enterprise1 = new Enterprise(
                1, "TechSolutions Brasil", "contato@techsolutions.com.br", "SP", "01310000",
                "Empresa especializada em desenvolvimento de software sob medida",
                [Competence.JAVA], "11222333000181", "Brasil"
        )
        Enterprise enterprise2 = new Enterprise(
                2, "Inovacao Digital LTDA", "rh@inovacaodigital.io", "MG", "30110010",
                "Consultoria focada em transformacao digital e arquitetura de sistemas",
                [Competence.GROOVY], "22333444000192", "Brasil"
        )
        database.getEnterprises() >> [enterprise1, enterprise2]

        when:
        List<Enterprise> result = enterpriseService.getAllEnterprises()

        then:
        result == [enterprise1, enterprise2]
    }

    def "get enterprise by id should return enterprise from database"() {
        given:
        Enterprise enterprise = new Enterprise(
                10, "TechSolutions Brasil", "contato@techsolutions.com.br", "SP", "01310000",
                "Empresa especializada em desenvolvimento de software sob medida",
                [Competence.JAVA, Competence.SPRING_FRAMEWORK], "11222333000181", "Brasil"
        )
        database.findEnterpriseById(10) >> enterprise

        when:
        Enterprise result = enterpriseService.getEnterpriseById(10)

        then:
        result == enterprise
    }
}
