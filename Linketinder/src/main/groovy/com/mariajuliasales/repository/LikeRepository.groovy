package com.mariajuliasales.repository

import com.mariajuliasales.model.LikedVacancy
import com.mariajuliasales.model.VacancyStatus

import java.sql.Connection

/**
 * Curtidas de candidato (candidate_like), de recrutador (recruiter_like) e da empresa (enterprise_like).
 * Todos os métodos rodam dentro da transação aberta pelo serviço, que guarda as regras (vaga OPEN, invariante
 * enterprise_like ⇔ recruiter_like, match). Nenhuma regra fica no SQL.
 * Os métodos devolvem só ids, status, boolean e dados de vaga, nunca dados pessoais.
 */
interface LikeRepository {

	/** Trava a linha da empresa (SELECT ... FOR UPDATE) até o fim da transação. Devolve false se a empresa não existe. */
	boolean lockEnterprise(Connection connection, int enterpriseId)

	/**
	 * Relê o status da vaga com SELECT ... FOR SHARE: até o fim da transação, ninguém muda nem apaga a vaga.
	 * Devolve null se a vaga não existe. Ordem das travas: sempre depois do lockEnterprise, nunca antes.
	 */
	VacancyStatus lockVacancyStatus(Connection connection, int vacancyId)

	boolean candidateLikeExists(Connection connection, int candidateId, int vacancyId)

	/** Idempotente. Devolve false se a curtida já existia. */
	boolean insertCandidateLike(Connection connection, int candidateId, int vacancyId)

	/** Devolve false se a curtida não existia. O match (C, V) cai em cascata pela FK. */
	boolean deleteCandidateLike(Connection connection, int candidateId, int vacancyId)

	/**
	 * Vagas curtidas pelo candidato, em qualquer status, em ordem crescente de id, com a flag de match.
	 * Lê só dados da vaga: colunas de vacancy, cidade/UF do endereço da vaga (vacancy.address_id) e nomes das
	 * competências da vaga (lista vazia se não houver). Nenhuma coluna de person. Só leitura, sem trava.
	 */
	List<LikedVacancy> likedVacanciesOf(Connection connection, int candidateId)

	boolean recruiterLikeExists(Connection connection, int recruiterId, int candidateId)

	/** Idempotente. Devolve false se a curtida já existia. */
	boolean insertRecruiterLike(Connection connection, int recruiterId, int candidateId)

	/** Devolve false se a curtida não existia. */
	boolean deleteRecruiterLike(Connection connection, int recruiterId, int candidateId)

	/** Algum outro recrutador da mesma empresa do recrutador informado curte o candidato. */
	boolean otherRecruiterOfEnterpriseLikes(Connection connection, int recruiterId, int candidateId)

	/** Ids dos candidatos curtidos pelo recrutador, em ordem crescente. */
	List<Integer> candidatesLikedBy(Connection connection, int recruiterId)

	boolean enterpriseLikeExists(Connection connection, int enterpriseId, int candidateId)

	/** Idempotente. Devolve false se a curtida já existia. */
	boolean insertEnterpriseLike(Connection connection, int enterpriseId, int candidateId)

	/** Devolve false se a curtida não existia. Não apaga matches: isso é feito pelo serviço. */
	boolean deleteEnterpriseLike(Connection connection, int enterpriseId, int candidateId)
}
