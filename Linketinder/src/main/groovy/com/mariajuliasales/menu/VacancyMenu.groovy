package com.mariajuliasales.menu

import com.mariajuliasales.dto.request.VacancyRequest
import com.mariajuliasales.mapper.VacancyMapper
import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.service.EntityFinder
import com.mariajuliasales.service.VacancyService

class VacancyMenu extends Menu {

	private final VacancyService vacancyService
	private final EntityFinder<Enterprise> enterpriseFinder
	private final CompetenceForm competenceForm

	VacancyMenu(ConsoleInput input, ConsoleView view, VacancyService vacancyService, EntityFinder<Enterprise> enterpriseFinder,
	CompetenceForm competenceForm) {
		super('Vagas', input, view)
		this.vacancyService = vacancyService
		this.enterpriseFinder = enterpriseFinder
		this.competenceForm = competenceForm
	}

	@Override
	protected List<MenuOption> getOptions() {
		[
			new MenuOption('Listar todas as vagas', this.&listVacancies),
			new MenuOption('Listar as vagas de uma empresa', this.&listEnterpriseVacancies),
			new MenuOption('Cadastrar vaga', this.&createVacancy),
			new MenuOption('Atualizar vaga', this.&updateVacancy),
			new MenuOption('Excluir vaga', this.&deleteVacancy)
		]
	}

	private void listVacancies() {
		showVacancies(vacancyService.findAll())
	}

	private void listEnterpriseVacancies() {
		Enterprise enterprise = findEnterprise()
		view.showMessage("Vagas da empresa ${enterprise.name}:")
		showVacancies(enterprise.vacancies)
	}

	private void createVacancy() {
		view.showMessage('Criando nova vaga...')
		Enterprise enterprise = findEnterprise()
		VacancyRequest request = new VacancyRequest(
				input.readText('Digite o título da vaga:'),
				input.readText('Digite a descrição da vaga:'),
				input.readText('Digite a cidade da vaga:'),
				input.readText('Digite o estado da vaga (ex: SP, MG):'),
				null,
				competenceForm.read('Digite as competências requeridas (separadas por vírgula):', [])*.name
				)
		Vacancy vacancy = vacancyService.create(VacancyMapper.toEntity(request, enterprise))
		view.showMessage("Vaga criada com sucesso! ID: ${vacancy.id}")
	}

	private void updateVacancy() {
		Vacancy vacancy = vacancyService.findById(input.readInt('Digite o ID da vaga:'))
		view.showMessage('Atualizando vaga (Enter mantém o valor atual)...')
		vacancy.title = input.readText('Título', vacancy.title)
		vacancy.description = input.readText('Descrição', vacancy.description)
		vacancy.address = new Address(
				city: input.readText('Cidade', vacancy.address?.city),
				state: input.readText('Estado', vacancy.address?.state)
				)
		vacancy.status = input.readStatus(vacancy.status)
		vacancy.competences = competenceForm.read('Competências', vacancy.competences)

		vacancyService.update(vacancy)
		view.showMessage('Vaga atualizada com sucesso!')
	}

	private void deleteVacancy() {
		vacancyService.delete(input.readInt('Digite o ID da vaga a excluir:'))
		view.showMessage('Vaga excluída com sucesso!')
	}

	private Enterprise findEnterprise() {
		enterpriseFinder.findById(input.readInt('Digite o ID da empresa:'))
	}

	private void showVacancies(List<Vacancy> vacancies) {
		List<String> lines = vacancies.collect { Vacancy vacancy ->
			ResponseFormatter.formatVacancy(VacancyMapper.toAnonymousResponse(vacancy))
		}
		view.showAll(lines, 'Nenhuma vaga cadastrada.')
	}
}
