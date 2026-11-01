package com.mariajuliasales.view

import com.mariajuliasales.controller.CompetenceCatalog

class CompetenceForm {

	private final ConsoleInput input
	private final ConsoleView view
	private final CompetenceCatalog catalog

	CompetenceForm(ConsoleInput input, ConsoleView view, CompetenceCatalog catalog) {
		this.input = input
		this.view = view
		this.catalog = catalog
	}

	List<String> read(String label, List<String> current) {
		view.showMessage("Competências disponíveis: ${catalog.findAll()*.name().join(', ')}")
		String typed = input.readText(current ? "${label} [${current.join(', ')}]:" : label)
		typed ? parse(typed) : current
	}

	private static List<String> parse(String typed) {
		List<String> names = typed.split(',')*.trim().findAll { String name -> name }
		names.unique { String name -> name.toLowerCase() }
	}
}
