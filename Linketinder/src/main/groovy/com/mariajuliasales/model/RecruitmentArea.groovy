package com.mariajuliasales.model

/**
 * Lista fixa de áreas de recrutamento. Espelha a tabela recruitment_area (migration 002):
 * o displayName é exatamente o valor da coluna name.
 */
enum RecruitmentArea {
	IT('TI'),
	HR('RH'),
	MARKETING('Marketing'),
	INFORMATION_SECURITY('Segurança da Informação'),
	FINANCE('Financeiro'),
	SALES('Comercial'),
	OPERATIONS('Operações')

	final String displayName

	RecruitmentArea(String displayName) {
		this.displayName = displayName
	}

	/** Busca pelo nome de exibição, sem diferenciar maiúsculas. Devolve null se o nome não está na lista. */
	static RecruitmentArea fromName(String name) {
		if (!name?.trim()) {
			return null
		}
		values().find { it.displayName.equalsIgnoreCase(name.trim()) }
	}

	@Override
	String toString() {
		displayName
	}
}
