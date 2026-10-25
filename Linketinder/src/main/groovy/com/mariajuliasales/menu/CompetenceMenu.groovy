package com.mariajuliasales.menu

import com.mariajuliasales.model.Competence
import com.mariajuliasales.service.CompetenceService

class CompetenceMenu extends Menu {

	private final CompetenceService competenceService

	CompetenceMenu(ConsoleInput input, CompetenceService competenceService) {
		super('Competências', input)
		this.competenceService = competenceService
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
		List<String> lines = competenceService.findAll().collect { Competence competence ->
			"${competence.id}. ${competence.name}".toString()
		}
		View.showAll(lines, 'Nenhuma competência cadastrada.')
	}

	private void createCompetence() {
		Competence competence = competenceService.create(input.readText('Digite o nome da competência:'))
		View.showMessage("Competência cadastrada com sucesso! ID: ${competence.id}")
	}

	private void renameCompetence() {
		Competence competence = competenceService.findById(input.readInt('Digite o ID da competência:'))
		competenceService.update(competence.id, input.readText('Novo nome', competence.name))
		View.showMessage('Competência atualizada com sucesso!')
	}

	private void deleteCompetence() {
		competenceService.delete(input.readInt('Digite o ID da competência a excluir:'))
		View.showMessage('Competência excluída com sucesso!')
	}
}
