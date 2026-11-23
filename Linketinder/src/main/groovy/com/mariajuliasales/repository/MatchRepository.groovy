package com.mariajuliasales.repository

import com.mariajuliasales.model.VacancyStatus

import java.sql.Connection

/**
 * Matches (vacancy_match). Todos os métodos rodam dentro da transação aberta pelo serviço, que decide quando um match
 * nasce ou cai (invariante enterprise_like ⇔ recruiter_like, curtida do candidato). Nenhuma regra fica no SQL.
 * Os métodos devolvem só ids e contagens, nunca dados pessoais.
 */
interface MatchRepository {

	/**
	 * Vagas da empresa que o candidato curtiu, em qualquer status: id da vaga → status, em ordem crescente de id.
	 * Não filtra OPEN: quem decide com quais vagas o match nasce é o serviço.
	 */
	Map<Integer, VacancyStatus> likedVacanciesOfEnterprise(Connection connection, int candidateId, int enterpriseId)

	/** Idempotente. Devolve false se o match já existia. Exige a candidate_like(C, V) (FK vacancy_match_like_fkey). */
	boolean insertMatch(Connection connection, int candidateId, int vacancyId)

	/** Apaga os matches do candidato com todas as vagas da empresa, em qualquer status. Devolve quantos apagou. */
	int deleteMatchesOfCandidateWithEnterprise(Connection connection, int candidateId, int enterpriseId)
}
