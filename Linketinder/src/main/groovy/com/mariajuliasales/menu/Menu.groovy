package com.mariajuliasales.menu

import com.mariajuliasales.exception.BusinessException

import java.sql.SQLException

abstract class Menu {

	private static final int EXIT_OPTION = 0

	protected final String title
	protected final ConsoleInput input

	Menu(String title, ConsoleInput input) {
		this.title = title
		this.input = input
	}

	protected abstract List<MenuOption> getOptions()

	protected void showOptions(List<String> labels) {
		View.showSubmenu(title, labels)
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
			View.showInvalidOption()
		}
	}

	private static void execute(MenuOption option) {
		try {
			option.action().call()
		} catch (BusinessException exception) {
			View.showError(exception.message)
		} catch (SQLException exception) {
			View.showError("Falha no banco de dados: ${exception.message}")
		}
	}
}
