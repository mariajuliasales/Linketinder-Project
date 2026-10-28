package com.mariajuliasales.menu

import com.mariajuliasales.mapper.CompetenceMapper
import com.mariajuliasales.model.Competence
import com.mariajuliasales.service.CompetenceCatalog

class CompetenceForm {

	private final ConsoleInput input
	private final ConsoleView view
	private final CompetenceCatalog catalog

	CompetenceForm(ConsoleInput input, ConsoleView view, CompetenceCatalog catalog) {
		this.input = input
		this.view = view
		this.catalog = catalog
	}

	List<Competence> read(String label, List<Competence> current) {
		view.showMessage("Competências disponíveis: ${CompetenceMapper.toNames(catalog.findAll()).join(', ')}")
		String typed = input.readText(current ? "${label} [${current.join(', ')}]:" : label)
		typed ? parse(typed) : current
	}

	private static List<Competence> parse(String typed) {
		List<String> names = typed.split(',')*.trim().findAll { String name -> name }
		CompetenceMapper.toEntities(names).unique()
	}
}
