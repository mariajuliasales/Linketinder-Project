package com.mariajuliasales.view

import com.mariajuliasales.controller.EnterpriseController
import com.mariajuliasales.controller.VacancyController
import com.mariajuliasales.dto.request.VacancyRequest
import com.mariajuliasales.dto.response.EnterpriseResponse
import com.mariajuliasales.dto.response.VacancyAnonymousResponse

class VacancyMenu extends Menu {

	private final VacancyController vacancyController
	private final EnterpriseController enterpriseController
	private final CompetenceForm competenceForm

	VacancyMenu(ConsoleInput input, ConsoleView view, VacancyController vacancyController,
	EnterpriseController enterpriseController, CompetenceForm competenceForm) {
		super('Vagas', input, view)
		this.vacancyController = vacancyController
		this.enterpriseController = enterpriseController
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
		showVacancies(vacancyController.findAll())
	}

	private void listEnterpriseVacancies() {
		EnterpriseResponse enterprise = findEnterprise()
		view.showMessage("Vagas da empresa ${enterprise.name()}:")
		showVacancies(enterprise.vacancies())
	}

	private void createVacancy() {
		view.showMessage('Criando nova vaga...')
		EnterpriseResponse enterprise = findEnterprise()
		VacancyRequest request = new VacancyRequest(
				input.readText('Digite o título da vaga:'),
				input.readText('Digite a descrição da vaga:'),
				input.readText('Digite a cidade da vaga:'),
				input.readText('Digite o estado da vaga (ex: SP, MG):'),
				null,
				competenceForm.read('Digite as competências requeridas (separadas por vírgula):', [])
				)
		VacancyAnonymousResponse vacancy = vacancyController.create(enterprise.id(), request)
		view.showMessage("Vaga criada com sucesso! ID: ${vacancy.id()}")
	}

	private void updateVacancy() {
		VacancyAnonymousResponse current = vacancyController.findById(input.readInt('Digite o ID da vaga:'))
		view.showMessage('Atualizando vaga (Enter mantém o valor atual)...')
		VacancyRequest request = new VacancyRequest(
				input.readText('Título', current.title()),
				input.readText('Descrição', current.description()),
				input.readText('Cidade', current.city()),
				input.readText('Estado', current.state()),
				input.readOption('Status', vacancyController.findStatuses(), current.status()),
				competenceForm.read('Competências', current.competences())
				)
		vacancyController.update(current.id(), request)
		view.showMessage('Vaga atualizada com sucesso!')
	}

	private void deleteVacancy() {
		vacancyController.delete(input.readInt('Digite o ID da vaga a excluir:'))
		view.showMessage('Vaga excluída com sucesso!')
	}

	private EnterpriseResponse findEnterprise() {
		enterpriseController.findById(input.readInt('Digite o ID da empresa:'))
	}

	private void showVacancies(List<VacancyAnonymousResponse> vacancies) {
		view.showAll(vacancies.collect { VacancyAnonymousResponse vacancy -> ResponseFormatter.formatVacancy(vacancy) },
		'Nenhuma vaga cadastrada.')
	}
}
