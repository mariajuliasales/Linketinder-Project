package com.mariajuliasales.menu

import com.mariajuliasales.model.Competence
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.EnterpriseService

class Menu {

    final Scanner scanner = new Scanner(System.in)

    final CandidateService candidateService
    final EnterpriseService enterpriseService


    Menu(CandidateService candidateService, EnterpriseService enterpriseService) {
        this.candidateService = candidateService
        this.enterpriseService = enterpriseService
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
                    candidateService.getAllCandidates()
                    break

                case 2:
                    println "Listando todas as empresas..."
                    enterpriseService.getAllEnterprises()
                    break

                case 3:
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
                    println "Digite as competências do candidato (separadas por vírgula):"
                    String competencesInput = scanner.nextLine()
                    int age = ageInput.trim().isInteger() ? ageInput.trim().toInteger() : 0

                    List<Competence> competences = parseCompetences(competencesInput)
                    try{
                        candidateService.create(0, name, email, state, cep, description, competences, cpf, age)

                    } catch(Exception e) {
                            println "Erro ao criar candidato: ${e.message}"
                            break
                        }
                    println "Candidato criado com sucesso!"
                    break
                case 4:
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

                    try {
                        enterpriseService.create(0, name, email, state, cep, description, competences, cnpj, country)
                    } catch(Exception e) {
                        println "algum erro ocorreu ao criar a empresa"
                        println "Erro ao criar empresa: ${e.message}"
                        break
                    }

                    println "Empresa cadastrada com sucesso!"
                    break
                case 5:
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
