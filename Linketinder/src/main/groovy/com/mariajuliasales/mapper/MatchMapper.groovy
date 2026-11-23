package com.mariajuliasales.mapper

import com.mariajuliasales.dto.response.CandidateMatchResponse
import com.mariajuliasales.dto.response.MatchedVacancyResponse
import com.mariajuliasales.dto.response.VacancyMatchResponse
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.CandidateMatches
import com.mariajuliasales.model.LikedVacancy
import com.mariajuliasales.model.LikedVacancyView
import com.mariajuliasales.model.Vacancy

/**
 * Monta as respostas de revelação campo a campo. Os dados identificáveis só entram quando há match;
 * CPF, nascimento, idade, endereço, senha, CNPJ e enterpriseId nunca entram.
 */
final class MatchMapper {

	static MatchedVacancyResponse toMatchedVacancyResponse(Vacancy vacancy) {
		if (vacancy == null) {
			return null
		}
		new MatchedVacancyResponse(vacancy.id, vacancy.title, vacancy.status?.name())
	}

	static List<MatchedVacancyResponse> toMatchedVacancyResponses(List<Vacancy> vacancies) {
		vacancies?.findAll { it != null }?.collect { Vacancy vacancy -> toMatchedVacancyResponse(vacancy) } ?: []
	}

	/**
	 * @param matchedVacancies só as vagas da empresa com vacancy_match para o candidato.
	 *        Vazia ou nula = sem match: nome e e-mail ficam nulos.
	 */
	static CandidateMatchResponse toCandidateMatchResponse(Candidate candidate, List<Vacancy> matchedVacancies) {
		if (candidate == null) {
			return null
		}
		List<MatchedVacancyResponse> matched = toMatchedVacancyResponses(matchedVacancies)
		boolean hasMatch = !matched.isEmpty()
		new CandidateMatchResponse(
				candidate.id,
				candidate.training,
				candidate.description,
				CompetenceMapper.toNames(candidate.competences),
				hasMatch ? candidate.name : null,
				hasMatch ? candidate.email : null,
				matched
				)
	}

	/** Repassa para toCandidateMatchResponse(Candidate, List<Vacancy>): vagas vazias = sem match. */
	static CandidateMatchResponse toCandidateMatchResponse(CandidateMatches matches) {
		if (matches == null) {
			return null
		}
		toCandidateMatchResponse(matches.candidate(), matches.vacancies())
	}

	/** @param matched true só quando existe vacancy_match entre o candidato e esta vaga. */
	static VacancyMatchResponse toVacancyMatchResponse(Vacancy vacancy, boolean matched) {
		if (vacancy == null) {
			return null
		}
		new VacancyMatchResponse(
				vacancy.id,
				vacancy.title,
				vacancy.description,
				vacancy.address?.city,
				vacancy.address?.state,
				vacancy.status?.name(),
				CompetenceMapper.toNames(vacancy.competences),
				matched ? vacancy.enterprise?.name : null,
				matched ? vacancy.enterprise?.email : null
				)
	}

	/**
	 * Vaga curtida pelo candidato ("Minhas curtidas"). Nome e e-mail da empresa só entram quando
	 * vacancy.matched() é verdadeiro; enterpriseId e CNPJ nunca entram.
	 */
	static VacancyMatchResponse toVacancyMatchResponse(LikedVacancyView view) {
		LikedVacancy liked = view?.vacancy()
		if (liked == null) {
			return null
		}
		boolean matched = liked.matched()
		new VacancyMatchResponse(
				liked.id(),
				liked.title(),
				liked.description(),
				liked.city(),
				liked.state(),
				liked.status()?.name(),
				liked.competences(),
				matched ? view.enterpriseName() : null,
				matched ? view.enterpriseEmail() : null
				)
	}
}
