package com.mariajuliasales.repository

import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus

import java.sql.Connection

/**
 * Matches (vacancy_match). Todos os métodos rodam dentro da transação aberta pelo serviço, que decide quando um match
 * nasce ou cai (invariante enterprise_like ⇔ recruiter_like, curtida do candidato). Nenhuma regra fica no SQL.
 * Os métodos devolvem só ids, contagens e dados de vaga (id, título, status), nunca dados pessoais.
 *
 * Os métodos de escrita e o likedVacanciesOfEnterprise precisam ser chamados dentro da transação do serviço, com a
 * empresa E já travada (LikeRepository#lockEnterprise). O matchedVacanciesByEnterprise é só leitura, sem trava. Ordem das travas: sempre enterprise antes de vacancy; nenhum fluxo trava vacancy e
 * depois enterprise.
 */
interface MatchRepository {

	/**
	 * Vagas da empresa que o candidato curtiu, em qualquer status: id da vaga → status, em ordem crescente de id.
	 * Não filtra OPEN: quem decide com quais vagas o match nasce é o serviço.
	 * Trava as vagas lidas (FOR SHARE OF v): até o fim da transação, elas não mudam de status nem somem.
	 */
	Map<Integer, VacancyStatus> likedVacanciesOfEnterprise(Connection connection, int candidateId, int enterpriseId)

	/** Idempotente. Devolve false se o match já existia. Exige a candidate_like(C, V) (FK vacancy_match_like_fkey). */
	boolean insertMatch(Connection connection, int candidateId, int vacancyId)

	/** Apaga os matches do candidato com todas as vagas da empresa, em qualquer status. Devolve quantos apagou. */
	int deleteMatchesOfCandidateWithEnterprise(Connection connection, int candidateId, int enterpriseId)

	/**
	 * Vagas da empresa com vacancy_match, agrupadas por candidate_id: candidatos e vagas em ordem crescente de id.
	 * Cada Vacancy traz só id, título e status (sem empresa, endereço ou competências). Sem colunas de person.
	 * Candidato sem match não aparece. Só leitura, sem trava.
	 */
	Map<Integer, List<Vacancy>> matchedVacanciesByEnterprise(Connection connection, int enterpriseId)
}
