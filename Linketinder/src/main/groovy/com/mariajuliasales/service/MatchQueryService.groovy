package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.CandidateMatches
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.MatchRepository
import com.mariajuliasales.repository.RecruiterRepository

import java.sql.Connection

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound

/**
 * Consultas de leitura sobre matches (revelação). Só leitura: withConnection, sem transação e sem trava.
 * As listas de revelação trazem só candidatos com ao menos um vacancy_match com vaga da empresa; quem não tem match
 * fica fora (continua só na listagem anônima). Duas consultas por chamada: vagas com match e candidatos.
 */
class MatchQueryService {

	private static final String ENTERPRISE = 'Empresa'
	private static final String RECRUITER = 'Recrutador'

	private final ConnectionManager connectionManager
	private final CandidateRepository candidateRepository
	private final EnterpriseRepository enterpriseRepository
	private final RecruiterRepository recruiterRepository
	private final MatchRepository matchRepository

	MatchQueryService(ConnectionManager connectionManager, CandidateRepository candidateRepository,
			EnterpriseRepository enterpriseRepository, RecruiterRepository recruiterRepository,
			MatchRepository matchRepository) {
		this.connectionManager = connectionManager
		this.candidateRepository = candidateRepository
		this.enterpriseRepository = enterpriseRepository
		this.recruiterRepository = recruiterRepository
		this.matchRepository = matchRepository
	}

	/** 404 se a empresa não existe. Candidatos e vagas em ordem crescente de id. */
	List<CandidateMatches> candidatesWithMatchesForEnterprise(int enterpriseId) {
		requireFound(enterpriseRepository.findById(enterpriseId), ENTERPRISE, enterpriseId)
		candidatesWithMatches(enterpriseId)
	}

	/** 404 se o recrutador não existe. Usa a empresa do recrutador. */
	List<CandidateMatches> candidatesWithMatchesForRecruiter(int recruiterId) {
		Recruiter recruiter = requireFound(recruiterRepository.findById(recruiterId), RECRUITER, recruiterId)
		candidatesWithMatches(recruiter.enterpriseId)
	}

	private List<CandidateMatches> candidatesWithMatches(int enterpriseId) {
		Map<Integer, List<Vacancy>> vacanciesByCandidate = connectionManager.withConnection { Connection connection ->
			matchRepository.matchedVacanciesByEnterprise(connection, enterpriseId)
		}
		if (!vacanciesByCandidate) {
			return []
		}
		// O SQL já ordena; a ordenação aqui garante a ordem mesmo que o repositório mude.
		candidateRepository.findAllByIds(vacanciesByCandidate.keySet())
				.findAll { Candidate candidate -> vacanciesByCandidate.containsKey(candidate.id) }
				.sort(false) { Candidate candidate -> candidate.id }
				.collect { Candidate candidate ->
					new CandidateMatches(candidate, vacanciesByCandidate[candidate.id].sort(false) { Vacancy vacancy -> vacancy.id })
				}
	}
}
