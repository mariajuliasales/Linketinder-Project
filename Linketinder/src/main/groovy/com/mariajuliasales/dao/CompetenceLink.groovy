package com.mariajuliasales.dao

enum CompetenceLink {

	CANDIDATE('candidate_competence', 'candidate_id'),
	ENTERPRISE('enterprise_competence', 'enterprise_id'),
	VACANCY('vacancy_competence', 'vacancy_id')

	final String table
	final String ownerColumn

	CompetenceLink(String table, String ownerColumn) {
		this.table = table
		this.ownerColumn = ownerColumn
	}
}
