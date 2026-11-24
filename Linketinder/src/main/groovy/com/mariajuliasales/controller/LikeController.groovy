package com.mariajuliasales.controller

import com.mariajuliasales.dto.response.CandidateMatchResponse
import com.mariajuliasales.dto.response.VacancyMatchResponse
import com.mariajuliasales.mapper.MatchMapper
import com.mariajuliasales.model.CandidateMatches
import com.mariajuliasales.model.LikedVacancyView
import com.mariajuliasales.service.CandidateLikeService
import com.mariajuliasales.service.MatchQueryService
import com.mariajuliasales.service.RecruiterLikeService

/**
 * Curtidas e listas de revelação. As listas devolvem só CandidateMatchResponse de candidatos com match com a empresa;
 * candidato sem match continua só em CandidateController.findAll (anônimo). "Minhas curtidas" do candidato devolve
 * VacancyMatchResponse, com nome e e-mail da empresa só nas vagas com match. Nunca devolve entidades.
 */
class LikeController {

	private final CandidateLikeService candidateLikeService
	private final RecruiterLikeService recruiterLikeService
	private final MatchQueryService matchQueryService

	LikeController(CandidateLikeService candidateLikeService, RecruiterLikeService recruiterLikeService,
			MatchQueryService matchQueryService) {
		this.candidateLikeService = candidateLikeService
		this.recruiterLikeService = recruiterLikeService
		this.matchQueryService = matchQueryService
	}

	void candidateLikes(int candidateId, int vacancyId) {
		candidateLikeService.like(candidateId, vacancyId)
	}

	void candidateUnlikes(int candidateId, int vacancyId) {
		candidateLikeService.unlike(candidateId, vacancyId)
	}

	void recruiterLikes(int recruiterId, int candidateId) {
		recruiterLikeService.like(recruiterId, candidateId)
	}

	void recruiterUnlikes(int recruiterId, int candidateId) {
		recruiterLikeService.unlike(recruiterId, candidateId)
	}

	List<CandidateMatchResponse> listCandidatesForRecruiter(int recruiterId) {
		toResponses(matchQueryService.candidatesWithMatchesForRecruiter(recruiterId))
	}

	List<CandidateMatchResponse> listCandidatesForEnterprise(int enterpriseId) {
		toResponses(matchQueryService.candidatesWithMatchesForEnterprise(enterpriseId))
	}

	List<VacancyMatchResponse> listLikedVacancies(int candidateId) {
		matchQueryService.listLikedVacancies(candidateId).collect { LikedVacancyView view ->
			MatchMapper.toVacancyMatchResponse(view)
		}
	}

	private static List<CandidateMatchResponse> toResponses(List<CandidateMatches> candidates) {
		candidates.collect { CandidateMatches matches -> MatchMapper.toCandidateMatchResponse(matches) }
	}
}
