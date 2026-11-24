package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.CandidateMatches
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.LikedVacancy
import com.mariajuliasales.model.LikedVacancyView
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.repository.CandidateRepository
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.LikeRepository
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
	private static final String CANDIDATE = 'Candidato'

	private final ConnectionManager connectionManager
	private final CandidateRepository candidateRepository
	private final EnterpriseRepository enterpriseRepository
	private final RecruiterRepository recruiterRepository
	private final MatchRepository matchRepository
	private final LikeRepository likeRepository

	MatchQueryService(ConnectionManager connectionManager, CandidateRepository candidateRepository,
			EnterpriseRepository enterpriseRepository, RecruiterRepository recruiterRepository,
			MatchRepository matchRepository, LikeRepository likeRepository) {
		this.connectionManager = connectionManager
		this.candidateRepository = candidateRepository
		this.enterpriseRepository = enterpriseRepository
		this.recruiterRepository = recruiterRepository
		this.matchRepository = matchRepository
		this.likeRepository = likeRepository
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

	/**
	 * Vagas curtidas pelo candidato, em qualquer status e em ordem crescente de id (ORDER BY do SQL).
	 * Nome e e-mail da empresa só nas vagas com match: a empresa é buscada uma vez por empresa distinta e nunca para
	 * vaga sem match. A entidade Enterprise (com CNPJ) não sai daqui. 404 se o candidato não existe.
	 */
	List<LikedVacancyView> listLikedVacancies(int candidateId) {
		requireFound(candidateRepository.findById(candidateId), CANDIDATE, candidateId)
		List<LikedVacancy> liked = connectionManager.withConnection { Connection connection ->
			likeRepository.likedVacanciesOf(connection, candidateId)
		}
		Map<Integer, Enterprise> enterprisesById = liked
				.findAll { LikedVacancy vacancy -> vacancy.matched() }
				.collect { LikedVacancy vacancy -> vacancy.enterpriseId() }
				.unique()
				.collectEntries { Integer enterpriseId -> [(enterpriseId): enterpriseRepository.findById(enterpriseId)] }
		liked.collect { LikedVacancy vacancy ->
			Enterprise enterprise = vacancy.matched() ? enterprisesById[vacancy.enterpriseId()] : null
			new LikedVacancyView(vacancy, enterprise?.name, enterprise?.email)
		}
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
