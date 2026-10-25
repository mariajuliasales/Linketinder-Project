package com.mariajuliasales.menu

class MainMenu extends Menu {

	private final List<MenuOption> options

	MainMenu(ConsoleInput input, CandidateMenu candidateMenu, EnterpriseMenu enterpriseMenu, VacancyMenu vacancyMenu,
	CompetenceMenu competenceMenu) {
		super('LinkeTinder', input)
		options = [
			new MenuOption('Candidatos', candidateMenu.&run),
			new MenuOption('Empresas', enterpriseMenu.&run),
			new MenuOption('Vagas', vacancyMenu.&run),
			new MenuOption('Competências', competenceMenu.&run)
		]
	}

	@Override
	protected List<MenuOption> getOptions() {
		options
	}

	@Override
	protected void showOptions(List<String> labels) {
		View.showMainMenu(labels)
	}

	@Override
	protected void onExit() {
		View.showMessage('Saindo do programa...')
	}
}
