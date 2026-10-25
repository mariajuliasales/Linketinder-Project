package com.mariajuliasales.service.validator

import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.util.AddressValidator

import static com.mariajuliasales.service.validator.Validation.hasText
import static com.mariajuliasales.service.validator.Validation.require

final class VacancyValidator {

	static void validate(Vacancy vacancy) {
		require(vacancy != null, "Dados da vaga não informados")
		require(hasText(vacancy.title), "Título da vaga é obrigatório")
		require(hasText(vacancy.description), "Descrição da vaga é obrigatória")
		require(vacancy.enterprise != null, "A vaga deve pertencer a uma empresa")
		require(AddressValidator.isValidLocation(vacancy.address), "Cidade e UF da vaga são obrigatórias")
		require(hasValidCompetences(vacancy), "A vaga deve ter pelo menos uma competência")
	}

	private static boolean hasValidCompetences(Vacancy vacancy) {
		vacancy.competences && vacancy.competences.every { it instanceof Competence }
	}
}
