package com.mariajuliasales.menu

class ConsoleView {

	private final PrintStream out

	ConsoleView(PrintStream out = System.out) {
		this.out = out
	}

	void showMainMenu(List<String> labels) {
		out.println ''
		out.println 'Bem vindo(a) ao LinkeTinder!'
		out.println 'Escolha uma opção:'
		showNumbered(labels)
		out.println '0. Sair do programa.'
	}

	void showSubmenu(String title, List<String> labels) {
		out.println ''
		out.println "--- ${title} ---"
		showNumbered(labels)
		out.println '0. Voltar'
	}

	void showAll(List<String> lines, String emptyMessage) {
		if (lines.isEmpty()) {
			out.println emptyMessage
		}
		lines.each { String line -> out.println line }
	}

	void showMessage(String message) {
		out.println message
	}

	void showError(String message) {
		out.println "Erro: ${message}"
	}

	void showInvalidOption() {
		out.println 'Opção inválida. Por favor, tente novamente.'
	}

	private void showNumbered(List<String> labels) {
		labels.eachWithIndex { String label, int index -> out.println "${index + 1}. ${label}" }
	}
}
