package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.util.DocumentNormalizer

import java.sql.Connection

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound
import static com.mariajuliasales.service.validator.Validation.require

/**
 * Curtida do recrutador em candidato. Guarda a invariante:
 * enterprise_like(E, C) existe se e somente se algum recrutador de E curte C. O banco não garante essa invariante.
 * O recrutador e o candidato carregados ficam só neste serviço: nenhum dado sai daqui.
 * Ordem das travas: sempre enterprise antes de vacancy.
 */
class RecruiterLikeService {

	private static final String RECRUITER = 'Recrutador'
	private static final String CANDIDATE = 'Candidato'
	private static final String SELF_LIKE_MESSAGE = 'Você não pode curtir o seu próprio perfil de candidato.'

	private final ConnectionManager connectionManager
	private final RecruiterRepository recruiterRepository
	private final CandidateRepository candidateRepository
	private final LikeRepository likeRepository
	private final MatchRepository matchRepository

	RecruiterLikeService(ConnectionManager connectionManager, RecruiterRepository recruiterRepository,
			CandidateRepository candidateRepository, LikeRepository likeRepository, MatchRepository matchRepository) {
		this.connectionManager = connectionManager
		this.recruiterRepository = recruiterRepository
		this.candidateRepository = candidateRepository
		this.likeRepository = likeRepository
		this.matchRepository = matchRepository
	}

	/**
	 * Antes da transação: recrutador e candidato existem (404) e não têm o mesmo CPF (400, a mensagem não traz o CPF).
	 * Na transação: trava E (404 do recrutador se E sumiu, porque o recrutador cai junto em cascata) → confere se E já
	 * curtia C, antes de gravar a curtida atual → grava a recruiter_like (idempotente) → se E ainda não curtia C, grava
	 * a enterprise_like e cria o match com cada vaga OPEN de E que C curtiu. A área do recrutador não filtra nada.
	 * Qualquer erro desfaz tudo.
	 */
	void like(int recruiterId, int candidateId) {
		Recruiter recruiter = requireFound(recruiterRepository.findById(recruiterId), RECRUITER, recruiterId)
		Candidate candidate = requireFound(candidateRepository.findById(candidateId), CANDIDATE, candidateId)
		require(!sameCpf(recruiter, candidate), SELF_LIKE_MESSAGE)
		int enterpriseId = recruiter.enterpriseId

		connectionManager.withTransaction { Connection connection ->
			if (!likeRepository.lockEnterprise(connection, enterpriseId)) {
				throw new EntityNotFoundException(RECRUITER, recruiterId)
			}
			boolean enterpriseAlreadyLikes = likeRepository.enterpriseLikeExists(connection, enterpriseId, candidateId)
			likeRepository.insertRecruiterLike(connection, recruiterId, candidateId)
			if (enterpriseAlreadyLikes) {
				return
			}
			likeRepository.insertEnterpriseLike(connection, enterpriseId, candidateId)
			openLikedVacancies(connection, candidateId, enterpriseId).each { int vacancyId ->
				matchRepository.insertMatch(connection, candidateId, vacancyId)
			}
		}
	}

	/** Só vagas OPEN ganham match novo; a candidate_like em vaga PAUSED/CLOSED continua, sem match. */
	private List<Integer> openLikedVacancies(Connection connection, int candidateId, int enterpriseId) {
		matchRepository.likedVacanciesOfEnterprise(connection, candidateId, enterpriseId)
				.findAll { Integer vacancyId, VacancyStatus status -> status == VacancyStatus.OPEN }
				.keySet()
				.toList()
	}

	private static boolean sameCpf(Recruiter recruiter, Candidate candidate) {
		String recruiterCpf = DocumentNormalizer.onlyDigits(recruiter.cpf)
		recruiterCpf && recruiterCpf == DocumentNormalizer.onlyDigits(candidate.cpf)
	}
}
