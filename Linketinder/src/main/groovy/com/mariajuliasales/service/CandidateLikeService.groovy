package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.VacancyRepository

import java.sql.Connection

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound
import static com.mariajuliasales.service.validator.Validation.require

/**
 * Curtida do candidato em vaga. O match (C, V) nasce na mesma transação da curtida quando a empresa dona da vaga já
 * curte o candidato (enterprise_like). A vaga e o candidato carregados ficam só neste serviço: nenhum dado sai daqui.
 * Ordem das travas: sempre enterprise antes de vacancy.
 */
class CandidateLikeService {

	private static final String CANDIDATE = 'Candidato'
	private static final String VACANCY = 'Vaga'

	private final ConnectionManager connectionManager
	private final CandidateRepository candidateRepository
	private final VacancyRepository vacancyRepository
	private final LikeRepository likeRepository
	private final MatchRepository matchRepository

	CandidateLikeService(ConnectionManager connectionManager, CandidateRepository candidateRepository,
			VacancyRepository vacancyRepository, LikeRepository likeRepository, MatchRepository matchRepository) {
		this.connectionManager = connectionManager
		this.candidateRepository = candidateRepository
		this.vacancyRepository = vacancyRepository
		this.likeRepository = likeRepository
		this.matchRepository = matchRepository
	}

	/**
	 * Antes da transação: candidato existe (404) e a vaga é carregada só para descobrir a empresa E (404).
	 * Na transação: trava E (404 da vaga se E sumiu, porque a vaga cai junto em cascata; a mensagem não revela E) → relê o status da vaga com FOR SHARE (404 se sumiu, 400 se não está OPEN) → grava a
	 * curtida (idempotente) → se E já curte o candidato, cria o match. Qualquer erro desfaz tudo.
	 */
	void like(int candidateId, int vacancyId) {
		requireFound(candidateRepository.findById(candidateId), CANDIDATE, candidateId)
		Vacancy vacancy = requireFound(vacancyRepository.findById(vacancyId), VACANCY, vacancyId)
		int enterpriseId = vacancy.enterprise.id

		connectionManager.withTransaction { Connection connection ->
			if (!likeRepository.lockEnterprise(connection, enterpriseId)) {
				throw new EntityNotFoundException(VACANCY, vacancyId)
			}
			VacancyStatus status = likeRepository.lockVacancyStatus(connection, vacancyId)
			if (status == null) {
				throw new EntityNotFoundException(VACANCY, vacancyId)
			}
			require(status == VacancyStatus.OPEN, 'Só é possível curtir vagas abertas')

			likeRepository.insertCandidateLike(connection, candidateId, vacancyId)
			if (likeRepository.enterpriseLikeExists(connection, enterpriseId, candidateId)) {
				matchRepository.insertMatch(connection, candidateId, vacancyId)
			}
		}
	}

	/**
	 * Funciona em qualquer status da vaga e é idempotente. O match (C, V) cai em cascata pela FK
	 * vacancy_match_like_fkey; a enterprise_like e os outros matches continuam.
	 */
	void unlike(int candidateId, int vacancyId) {
		connectionManager.withTransaction { Connection connection ->
			likeRepository.deleteCandidateLike(connection, candidateId, vacancyId)
		}
	}
}
