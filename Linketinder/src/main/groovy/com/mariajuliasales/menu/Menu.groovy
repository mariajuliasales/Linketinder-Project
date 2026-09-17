package com.mariajuliasales.menu

import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.mapper.CandidateMapper
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService

class Menu {

    final Scanner scanner = new Scanner(System.in)

    final CandidateService candidateService
    final EnterpriseService enterpriseService
    final VacancyService vacancyService

    Menu(CandidateService candidateService, EnterpriseService enterpriseService, VacancyService vacancyService) {
        this.candidateService = candidateService
        this.enterpriseService = enterpriseService
        this.vacancyService = vacancyService
    }

    def init() {

        def opc = 1
        int choice

        while (opc != -1) {

            View.showMenu()
            choice = scanner.nextInt()
            println(choice)


            switch (choice) {
                case 1:
                    List<CandidateAnonymousResponse> candidates = candidateService.getAllCandidates()
                            ?.collect { CandidateMapper.toCandidateAnonymousResponse(it) } ?: []
                    candidates.each { println it }
                    break

                case 2:
                    println "Listando todas as empresas..."
                    enterpriseService.getAllEnterprises()
                    break

                case 3:
                    println "Listando todas as vagas..."
                    vacancyService.findAll().each {println it.viewVacancyAnonymous()}
                    break

                case 4:
                    scanner.nextLine()
                    println "Criando novo candidato..."
                    println "Digite o nome do candidato:"
                    String name = scanner.nextLine()
                    println "Digite o email do candidato:"
                    String email = scanner.nextLine()
                    println "Digite o estado do candidato:"
                    String state = scanner.nextLine()
                    println "Digite o CEP do candidato:"
                    String cep = scanner.nextLine()
                    println "Digite a descrição do candidato:"
                    String description = scanner.nextLine()
                    println "Digite o CPF do candidato:"
                    String cpf = scanner.nextLine()
                    println "Digite a idade do candidato:"
                    String ageInput = scanner.nextLine()
                    int age = ageInput.trim().isInteger() ? ageInput.trim().toInteger() : 0

                    println "Digite as competências do candidato (separadas por vírgula):"
                    String competencesInput = scanner.nextLine()

                    List<Competence> competences = parseCompetences(competencesInput)

                    CandidateRequest candidateRequest = new CandidateRequest(10, name, email, state, cep, description, competences, cpf, age)


                    try{
                        candidateService.create(CandidateMapper.toCandidate(candidateRequest))

                    } catch(Exception e) {
                            println "Erro ao criar candidato: ${e.message}"
                            break
                        }
                    println "Candidato criado com sucesso!"
                    break
                case 5:
                    scanner.nextLine()
                    println "Criando nova empresa..."
                    println "Digite o nome da empresa:"
                    String name = scanner.nextLine()

                    println "Digite o email da empresa:"
                    String email = scanner.nextLine()

                    println "Digite o estado da empresa (ex: SP, MG):"
                    String state = scanner.nextLine()

                    println "Digite o CEP da empresa:"
                    String cep = scanner.nextLine()

                    println "Digite a descrição da empresa:"
                    String description = scanner.nextLine()

                    println "Digite o CNPJ da empresa:"
                    String cnpj = scanner.nextLine()

                    println "Digite o país da empresa:"
                    String country = scanner.nextLine()

                    println "Digite as competências procuradas (separadas por vírgula):"
                    String competencesInput = scanner.nextLine()

                    List<Competence> competences = parseCompetences(competencesInput)

                    Enterprise enterprise = new Enterprise(0, name, email, state, cep, description, competences, cnpj, country)

                    try {
                        enterpriseService.create(enterprise)
                    } catch(Exception e) {
                        println "Erro ao criar empresa: ${e.message}"
                        break
                    }

                    println "Empresa cadastrada com sucesso!"
                    break

                case 6:
                    scanner.nextLine()
                    println "Criando nova vaga..."
                    println "Digite o título da vaga:"
                    String title = scanner.nextLine()

                    println "Digite a descrição da vaga:"
                    String description = scanner.nextLine()

                    println "Digite as competências requeridas (separadas por vírgula):"
                    String competencesInput = scanner.nextLine()

                    List<Competence> competences = parseCompetences(competencesInput)

                    println "Digite o ID da empresa associada à vaga:"
                    int enterpriseId = scanner.nextInt()
                    Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseId)
                    scanner.nextLine()

                    Vacancy vacancy = new Vacancy(0, title, description, competences, enterprise)

                    try {
                        vacancyService.create(vacancy)
                        println "Vaga criada com sucesso!"
                    } catch(Exception e) {
                        println "Erro ao criar vaga: ${e.message}"
                        break
                    }
                    break

                case 7:
                    println "Saindo do programa..."
                    opc = -1
                    break

                default:
                    println "Opção inválida. Por favor, tente novamente."
            }


        }
    }

    private static List<Competence> parseCompetences(String entrada) {
        List<Competence> competences = []
        if (!entrada?.trim()) {
            return competences
        }
        entrada.split(',').each { nome ->
            String chave = nome.trim().toUpperCase().replace(' ', '_')
            try {
                competences << Competence.valueOf(chave)
            } catch (IllegalArgumentException ignored) {
                println "Competência '${nome.trim()}' não reconhecida, ignorada."
            }
        }
        competences
    }

}
