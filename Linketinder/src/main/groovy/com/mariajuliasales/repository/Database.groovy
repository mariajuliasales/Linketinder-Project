package com.mariajuliasales.repository

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise

class Database {

    private final List<Candidate> candidates = []
    private final List<Enterprise> enterprises = []

    private int candidateIdCounter = 1
    private int enterpriseIdCounter = 1


    Database() {
        initializeCandidateData()
        initializeEnterpriseData()
    }

    // ----- Candidate Methods

    List<Candidate> getCandidates() {
        if (candidates.isEmpty()) {
            println "Candidato não encontrado"
        }
        candidates
    }

    Candidate createCandidate(Candidate candidate) {
        candidate.id = generateIdCandidate()
        candidates << candidate
        candidate
    }

    Candidate findCandidateById(int id) {
        candidates.find { it.id == id }
    }

    // ----- Enterprise Methods

    List<Enterprise> getEnterprises() {
        if (enterprises.isEmpty()) {
            println "Empresa não encontrada"
        }
        enterprises
    }

    Enterprise createEnterprise(Enterprise enterprise) {
        enterprise.id = generateIdEnterprise()
        enterprises << enterprise
        enterprises
    }

    Enterprise findEnterpriseById(int id) {
        enterprises.find { it.id == id }
    }


    private int generateIdCandidate() {
        candidateIdCounter++
    }

    private int generateIdEnterprise() {
        enterpriseIdCounter++

    }

    // ----- Initial data

    private void initializeCandidateData() {

        candidates << new Candidate(generateIdCandidate(), "Maria Julia Sales", "mariajuliasales@gmail.com", "SP",
                "36700386", "Software Engineer", [Competence.JAVA, Competence.GROOVY], "123.456.789-00", 24)

        candidates << new Candidate(generateIdCandidate(), "Lucas Andrade Silveira", "lucas.andrade@gmail.com", "MG",
                "30110010", "Backend Developer", [Competence.JAVA, Competence.SPRING_FRAMEWORK], "38459201080", 26)

        candidates << new Candidate(generateIdCandidate(), "Beatriz Lima Rocha", "beatriz.rocha@outlook.com", "RJ",
                "22041001", "Full Stack Engineer", [Competence.GROOVY, Competence.JAVASCRIPT], "83741920040", 29)

        candidates << new Candidate(generateIdCandidate(), "Gabriel Santos Oliveira", "gabriel.santos@dev.com", "PR",
                "80010000", "Data Engineer", [Competence.PYTHON, Competence.SQL, Competence.DOCKER], "19283746030", 23)

        candidates << new Candidate(generateIdCandidate(), "Fernanda Costa Souza", "fernanda.souza@gmail.com", "SC",
                "88010000", "DevOps Engineer", [Competence.DOCKER, Competence.ANGULAR, Competence.JAVASCRIPT], "56473829000", 31)

        candidates << new Candidate(generateIdCandidate(), "Thiago Martins Ribeiro", "thiago.martins@yahoo.com", "SP",
                "01310000", "Frontend Developer", [Competence.REACT, Competence.JAVASCRIPT], "91827364020", 27)

    }

    private void initializeEnterpriseData() {
        enterprises << new Enterprise(generateIdEnterprise(), "TechSolutions Brasil", "contato@techsolutions.com.br", "SP",
                "01310000", "Empresa especializada em desenvolvimento de software sob medida", [Competence.JAVA, Competence.SPRING_FRAMEWORK], "11222333000181", "Brasil")

        enterprises << new Enterprise(generateIdEnterprise(), "Inovacao Digital LTDA", "rh@inovacaodigital.io", "MG",
                "30110010", "Consultoria focada em transformacao digital e arquitetura de sistemas", [Competence.GROOVY, Competence.DOCKER], "22333444000192", "Brasil")

        enterprises << new Enterprise(generateIdEnterprise(), "DataAnalytics Global", "carreiras@dataanalytics.com", "RJ",
                "22041001", "Lider em solucoes de inteligência de dados e Big Data", [Competence.PYTHON, Competence.SQL], "33444555000103", "Brasil")

        enterprises << new Enterprise(generateIdEnterprise(), "CloudScale Services", "jobs@cloudscale.com", "SC",
                "88010000", "Plataforma multicloud e gerenciamento de infraestrutura distribuida", [Competence.JAVA, Competence.DOCKER], "44555666000114", "Brasil")

        enterprises << new Enterprise(generateIdEnterprise(), "FrontCraft Studios", "talent@frontcraft.dev", "PR",
                "80010000", "Studio focado na construcao de experiencias web modernas e acessiveis", [Competence.REACT, Competence.JAVASCRIPT], "55666777000125", "Brasil")

        enterprises << new Enterprise(generateIdEnterprise(), "Nexus Software House", "vagas@nexussoftware.com.br", "RS",
                "90010000", "Desenvolvimento de produtos digitais de alta performance e SaaS", [Competence.JAVA, Competence.SPRING_FRAMEWORK], "66777888000136", "Brasil")

    }

}