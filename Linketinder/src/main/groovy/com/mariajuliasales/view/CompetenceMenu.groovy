package com.mariajuliasales.view

import com.mariajuliasales.controller.CompetenceController
import com.mariajuliasales.dto.response.CompetenceResponse

class CompetenceMenu extends Menu {

	private final CompetenceController competenceController

	CompetenceMenu(ConsoleInput input, ConsoleView view, CompetenceController competenceController) {
		super('Competências', input, view)
		this.competenceController = competenceController
	}

	@Override
	protected List<MenuOption> getOptions() {
		[
			new MenuOption('Listar competências', this.&listCompetences),
			new MenuOption('Cadastrar competência', this.&createCompetence),
			new MenuOption('Renomear competência', this.&renameCompetence),
			new MenuOption('Excluir competência', this.&deleteCompetence)
		]
	}

	private void listCompetences() {
		List<String> lines = competenceController.findAll().collect { CompetenceResponse competence ->
			"${competence.id()}. ${competence.name()}".toString()
		}
		view.showAll(lines, 'Nenhuma competência cadastrada.')
	}

	private void createCompetence() {
		CompetenceResponse competence = competenceController.create(input.readText('Digite o nome da competência:'))
		view.showMessage("Competência cadastrada com sucesso! ID: ${competence.id()}")
	}

	private void renameCompetence() {
		CompetenceResponse competence = competenceController.findById(input.readInt('Digite o ID da competência:'))
		competenceController.update(competence.id(), input.readText('Novo nome', competence.name()))
		view.showMessage('Competência atualizada com sucesso!')
	}

	private void deleteCompetence() {
		competenceController.delete(input.readInt('Digite o ID da competência a excluir:'))
		view.showMessage('Competência excluída com sucesso!')
	}
}
