package com.mariajuliasales.model

/**
 * Vaga curtida por um candidato, com a flag de match. Só dados da própria vaga (cidade/UF do endereço da vaga) e o id da
 * empresa dona (uso interno, para buscar nome/e-mail só quando há match). Não carrega nome, e-mail nem CNPJ.
 * Imutável: competências copiadas, nunca nulas. Nunca é serializado.
 */
record LikedVacancy(
		Integer id,
		String title,
		String description,
		String city,
		String state,
		VacancyStatus status,
		List<String> competences,
		Integer enterpriseId,
		boolean matched
		) {

	LikedVacancy {
		competences = List.copyOf(competences ?: [])
	}
}
