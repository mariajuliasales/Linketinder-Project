package com.mariajuliasales.menu

import com.mariajuliasales.mapper.CompetenceMapper
import com.mariajuliasales.model.Competence
import com.mariajuliasales.service.CompetenceService

class CompetenceForm {

	private final ConsoleInput input
	private final CompetenceService competenceService

	CompetenceForm(ConsoleInput input, CompetenceService competenceService) {
		this.input = input
		this.competenceService = competenceService
	}

	List<Competence> read(String label, List<Competence> current) {
		println "Competências disponíveis: ${CompetenceMapper.toNames(competenceService.findAll()).join(', ')}"
		String typed = input.readText(current ? "${label} [${current.join(', ')}]:" : label)
		typed ? parse(typed) : current
	}

	private static List<Competence> parse(String typed) {
		List<String> names = typed.split(',')*.trim().findAll { String name -> name }
		CompetenceMapper.toEntities(names).unique()
	}
}
