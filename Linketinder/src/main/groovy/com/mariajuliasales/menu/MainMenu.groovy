package com.mariajuliasales.menu

class MainMenu extends Menu {

	private final List<MenuOption> options

	MainMenu(ConsoleInput input, ConsoleView view, List<Menu> submenus) {
		super('LinkeTinder', input, view)
		options = submenus.collect { Menu submenu -> new MenuOption(submenu.title, submenu.&run) }
	}

	@Override
	protected List<MenuOption> getOptions() {
		options
	}

	@Override
	protected void showOptions(List<String> labels) {
		view.showMainMenu(labels)
	}

	@Override
	protected void onExit() {
		view.showMessage('Saindo do programa...')
	}
}
