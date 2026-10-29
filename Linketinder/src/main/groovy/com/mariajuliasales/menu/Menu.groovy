package com.mariajuliasales.menu

import com.mariajuliasales.exception.BusinessException

import java.sql.SQLException

abstract class Menu {

	private static final int EXIT_OPTION = 0

	final String title
	protected final ConsoleInput input
	protected final ConsoleView view

	Menu(String title, ConsoleInput input, ConsoleView view) {
		this.title = title
		this.input = input
		this.view = view
	}

	protected abstract List<MenuOption> getOptions()

	protected void showOptions(List<String> labels) {
		view.showSubmenu(title, labels)
	}

	protected void onExit() {
	}

	void run() {
		List<MenuOption> options = getOptions()
		int choice = readChoice(options)
		while (choice != EXIT_OPTION) {
			execute(options[choice - 1])
			choice = readChoice(options)
		}
		onExit()
	}

	private int readChoice(List<MenuOption> options) {
		while (true) {
			showOptions(options*.label())
			int choice = input.readInt('')
			if (choice == EXIT_OPTION || choice in 1..options.size()) {
				return choice
			}
			view.showInvalidOption()
		}
	}

	private void execute(MenuOption option) {
		try {
			option.action().call()
		} catch (BusinessException exception) {
			view.showError(exception.message)
		} catch (SQLException exception) {
			view.showError("Falha no banco de dados: ${exception.message}")
		}
	}
}
