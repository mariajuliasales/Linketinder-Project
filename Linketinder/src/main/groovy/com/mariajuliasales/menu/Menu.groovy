package com.mariajuliasales.menu

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.mapper.AddressMapper
import com.mariajuliasales.mapper.CandidateMapper
import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.CompetenceService
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class Menu {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern('dd/MM/yyyy')

    final Scanner scanner = new Scanner(System.in)

    final CandidateService candidateService
    final EnterpriseService enterpriseService
    final VacancyService vacancyService
    final CompetenceService competenceService

    Menu(CandidateService candidateService, EnterpriseService enterpriseService, VacancyService vacancyService,
         CompetenceService competenceService) {
        this.candidateService = candidateService
        this.enterpriseService = enterpriseService
        this.vacancyService = vacancyService
        this.competenceService = competenceService
    }

    def init() {
        boolean running = true
        while (running) {
            View.showMenu()
            switch (readInt("")) {
                case 1: candidateMenu(); break
                case 2: enterpriseMenu(); break
                case 3: vacancyMenu(); break
                case 4: competenceMenu(); break
                case 0:
                    println "Saindo do programa..."
                    running = false
                    break
                default:
                    println "Opção inválida. Por favor, tente novamente."
            }
        }
    }

    // Candidatos

    private void candidateMenu() {
        List<String> options = ["Listar candidatos (perfil anônimo)", "Ver perfil completo de um candidato",
                                "Cadastrar candidato", "Atualizar candidato", "Excluir candidato"]
        submenu("Candidatos", options) { int option ->
            switch (option) {
                case 1:
                    List<Candidate> candidates = candidateService.getAllCandidates()
                    if (!candidates) println "Nenhum candidato cadastrado."
                    candidates.each { println CandidateMapper.toCandidateAnonymousResponse(it) }
                    break
                case 2:
                    Candidate candidate = findCandidate()
                    if (candidate) println CandidateMapper.toCandidateResponse(candidate)
                    break
                case 3: createCandidate(); break
                case 4: updateCandidate(); break
                case 5:
                    candidateService.delete(readInt("Digite o ID do candidato a excluir:"))
                    println "Candidato excluído com sucesso!"
                    break
            }
        }
    }

    private void createCandidate() {
        println "Criando novo candidato..."
        CandidateRequest request = new CandidateRequest(
                readText("Digite o nome do candidato:"),
                readText("Digite o email do candidato:"),
                readText("Digite a senha (mínimo 6 caracteres):"),
                readAddress("Endereço do candidato:", null),
                readText("Digite a descrição do candidato:"),
                readCompetences("Digite as competências do candidato (separadas por vírgula):", [])*.name,
                readText("Digite o CPF do candidato:"),
                readDate("Digite a data de nascimento (dd/mm/aaaa):", null),
                readText("Digite a formação do candidato:")
        )
        Candidate candidate = candidateService.create(CandidateMapper.toCandidate(request))
        println "Candidato criado com sucesso! ID: ${candidate.id}"
    }

    private void updateCandidate() {
        Candidate candidate = findCandidate()
        if (!candidate) return

        println "Atualizando candidato (Enter mantém o valor atual)..."
        candidate.name = readText("Nome", candidate.name)
        candidate.email = readText("Email", candidate.email)
        candidate.password = readText("Nova senha (Enter mantém a atual):")
        candidate.address = AddressMapper.toAddress(readAddress("Endereço:", candidate.address))
        candidate.description = readText("Descrição", candidate.description)
        candidate.cpf = readText("CPF", candidate.cpf)
        candidate.birthDate = readDate("Data de nascimento", candidate.birthDate)
        candidate.training = readText("Formação", candidate.training)
        candidate.competences = readCompetences("Competências", candidate.competences)

        candidateService.update(candidate)
        println "Candidato atualizado com sucesso!"
    }

    private Candidate findCandidate() {
        Candidate candidate = candidateService.getCandidateById(readInt("Digite o ID do candidato:"))
        if (!candidate) println "Candidato não encontrado."
        candidate
    }


    // Empresas

    private void enterpriseMenu() {
        List<String> options = ["Listar empresas", "Ver dados completos de uma empresa",
                                "Cadastrar empresa", "Atualizar empresa", "Excluir empresa (e suas vagas)"]
        submenu("Empresas", options) { int option ->
            switch (option) {
                case 1:
                    List<Enterprise> enterprises = enterpriseService.getAllEnterprises()
                    if (!enterprises) println "Nenhuma empresa cadastrada."
                    enterprises.each { println it.viewProfileAnonymous() }
                    break
                case 2:
                    Enterprise enterprise = findEnterprise()
                    if (enterprise) {
                        println "Empresa ${enterprise.id}: ${enterprise.name} | CNPJ: ${enterprise.cnpj} | " +
                                "Email: ${enterprise.email} | Endereço: ${AddressMapper.toAddressResponse(enterprise.address)} | " +
                                "Descrição: ${enterprise.description} | " +
                                "Competências: ${enterprise.competences.join(', ')}"
                        println "Vagas (${enterprise.vacancies.size()}):"
                        enterprise.vacancies.each { println "  ${it.viewVacancyAnonymous()}" }
                    }
                    break
                case 3: createEnterprise(); break
                case 4: updateEnterprise(); break
                case 5:
                    enterpriseService.delete(readInt("Digite o ID da empresa a excluir:"))
                    println "Empresa excluída com sucesso!"
                    break
            }
        }
    }

    private void createEnterprise() {
        println "Criando nova empresa..."
        Enterprise enterprise = new Enterprise(
                name: readText("Digite o nome da empresa:"),
                email: readText("Digite o email corporativo da empresa:"),
                password: readText("Digite a senha (mínimo 6 caracteres):"),
                cnpj: readText("Digite o CNPJ da empresa:"),
                address: AddressMapper.toAddress(readAddress("Endereço da empresa:", null)),
                description: readText("Digite a descrição da empresa:"),
                competences: readCompetences("Digite as competências procuradas (separadas por vírgula):", [])
        )
        enterpriseService.create(enterprise)
        println "Empresa cadastrada com sucesso! ID: ${enterprise.id}"
    }

    private void updateEnterprise() {
        Enterprise enterprise = findEnterprise()
        if (!enterprise) return

        println "Atualizando empresa (Enter mantém o valor atual)..."
        enterprise.name = readText("Nome", enterprise.name)
        enterprise.email = readText("Email", enterprise.email)
        enterprise.password = readText("Nova senha (Enter mantém a atual):")
        enterprise.cnpj = readText("CNPJ", enterprise.cnpj)
        enterprise.address = AddressMapper.toAddress(readAddress("Endereço:", enterprise.address))
        enterprise.description = readText("Descrição", enterprise.description)
        enterprise.competences = readCompetences("Competências", enterprise.competences)

        enterpriseService.update(enterprise)
        println "Empresa atualizada com sucesso!"
    }

    private Enterprise findEnterprise() {
        Enterprise enterprise = enterpriseService.getEnterpriseById(readInt("Digite o ID da empresa:"))
        if (!enterprise) println "Empresa não encontrada."
        enterprise
    }


    // Vagas (CRUD usado pelas empresas)

    private void vacancyMenu() {
        List<String> options = ["Listar todas as vagas", "Listar as vagas de uma empresa",
                                "Cadastrar vaga", "Atualizar vaga", "Excluir vaga"]
        submenu("Vagas", options) { int option ->
            switch (option) {
                case 1:
                    List<Vacancy> vacancies = vacancyService.findAll()
                    if (!vacancies) println "Nenhuma vaga cadastrada."
                    vacancies.each { println it.viewVacancyAnonymous() }
                    break
                case 2:
                    Enterprise enterprise = findEnterprise()
                    if (enterprise) {
                        // As vagas já vêm carregadas com a empresa (relação 1:N)
                        println "Vagas da empresa ${enterprise.name}:"
                        if (!enterprise.vacancies) println "Nenhuma vaga cadastrada."
                        enterprise.vacancies.each { println it.viewVacancyAnonymous() }
                    }
                    break
                case 3: createVacancy(); break
                case 4: updateVacancy(); break
                case 5:
                    vacancyService.delete(readInt("Digite o ID da vaga a excluir:"))
                    println "Vaga excluída com sucesso!"
                    break
            }
        }
    }

    private void createVacancy() {
        println "Criando nova vaga..."
        Enterprise enterprise = findEnterprise()
        if (!enterprise) return

        Vacancy vacancy = new Vacancy(
                title: readText("Digite o título da vaga:"),
                description: readText("Digite a descrição da vaga:"),
                address: new Address(
                        city: readText("Digite a cidade da vaga:"),
                        state: readText("Digite o estado da vaga (ex: SP, MG):")
                ),
                competences: readCompetences("Digite as competências requeridas (separadas por vírgula):", []),
                enterprise: enterprise
        )
        vacancyService.create(vacancy)
        println "Vaga criada com sucesso! ID: ${vacancy.id}"
    }

    private void updateVacancy() {
        Vacancy vacancy = vacancyService.findById(readInt("Digite o ID da vaga:"))

        println "Atualizando vaga (Enter mantém o valor atual)..."
        vacancy.title = readText("Título", vacancy.title)
        vacancy.description = readText("Descrição", vacancy.description)
        vacancy.address = new Address(
                city: readText("Cidade", vacancy.address?.city),
                state: readText("Estado", vacancy.address?.state)
        )
        vacancy.status = readStatus(vacancy.status)
        vacancy.competences = readCompetences("Competências", vacancy.competences)

        vacancyService.update(vacancy)
        println "Vaga atualizada com sucesso!"
    }


    // Competências

    private void competenceMenu() {
        List<String> options = ["Listar competências", "Cadastrar competência", "Renomear competência",
                                "Excluir competência"]
        submenu("Competências", options) { int option ->
            switch (option) {
                case 1:
                    competenceService.findAll().each { println "${it.id}. ${it.name}" }
                    break
                case 2:
                    Competence competence = competenceService.create(readText("Digite o nome da competência:"))
                    println "Competência cadastrada com sucesso! ID: ${competence.id}"
                    break
                case 3:
                    Competence competence = competenceService.findById(readInt("Digite o ID da competência:"))
                    competenceService.update(competence.id, readText("Novo nome", competence.name))
                    println "Competência atualizada com sucesso!"
                    break
                case 4:
                    competenceService.delete(readInt("Digite o ID da competência a excluir:"))
                    println "Competência excluída com sucesso!"
                    break
            }
        }
    }


    // Teclado

    private void submenu(String title, List<String> options, Closure action) {
        while (true) {
            View.showSubmenu(title, options)
            int option = readInt("")
            if (option == 0) return
            if (option < 0 || option > options.size()) {
                println "Opção inválida. Por favor, tente novamente."
                continue
            }
            try {
                action(option)
            } catch (NoSuchElementException e) {
                throw e
            } catch (Exception e) {
                println "Erro: ${e.message}"
            }
        }
    }

    private String readText(String prompt) {
        if (prompt) println prompt
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("Fim da entrada")
        }
        scanner.nextLine().trim()
    }

    private String readText(String label, String current) {
        String value = readText(current ? "${label} [${current}]:" : "${label}:")
        value ?: current
    }

    private AddressRequest readAddress(String title, Address current) {
        println title
        new AddressRequest(
                readText("CEP", current?.cep),
                readText("Rua (opcional)", current?.street),
                readText("Número (opcional)", current?.number),
                readText("Complemento (opcional)", current?.complement),
                readText("Bairro (opcional)", current?.neighborhood),
                readText("Cidade", current?.city),
                readText("Estado (ex: SP, MG)", current?.state),
                readText("País", current?.country ?: 'Brasil')
        )
    }

    private int readInt(String prompt) {
        while (true) {
            String value = readText(prompt)
            if (value.isInteger()) {
                return value.toInteger()
            }
            println "Digite um número inteiro."
        }
    }

    private LocalDate readDate(String label, LocalDate current) {
        while (true) {
            String value = current ? readText("${label} [${current.format(DATE_FORMAT)}]:") : readText(label)
            if (!value && current) {
                return current
            }
            try {
                return LocalDate.parse(value, DATE_FORMAT)
            } catch (DateTimeParseException ignored) {
                println "Data inválida. Use o formato dd/mm/aaaa."
            }
        }
    }

    private VacancyStatus readStatus(VacancyStatus current) {
        while (true) {
            String value = readText("Status (OPEN, PAUSED, CLOSED) [${current}]:")
            if (!value) {
                return current
            }
            try {
                return VacancyStatus.valueOf(value.toUpperCase())
            } catch (IllegalArgumentException ignored) {
                println "Status inválido."
            }
        }
    }

    private List<Competence> readCompetences(String label, List<Competence> current) {
        println "Competências disponíveis: ${competenceService.findAll()*.name.join(', ')}"
        String prompt = current ? "${label} [${current.join(', ')}]:" : label
        String input = readText(prompt)
        if (!input) {
            return current
        }
        input.split(',')*.trim().findAll { it }.collect { new Competence(it) }.unique()
    }

}