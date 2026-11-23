package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.RecruiterRequest
import com.mariajuliasales.dto.response.RecruiterResponse
import com.mariajuliasales.dto.response.VacancyAnonymousResponse
import com.mariajuliasales.mapper.RecruiterMapper
import com.mariajuliasales.mapper.VacancyMapper
import com.mariajuliasales.service.RecruiterService
import com.mariajuliasales.service.VacancyService

/**
 * Devolve só RecruiterResponse (sem cpf, birthDate, age e password), nunca a entidade Recruiter.
 * As vagas da empresa são somente leitura para o recrutador e saem como VacancyAnonymousResponse, sem dados da empresa.
 */
class RecruiterController {

	private final RecruiterService recruiterService
	private final VacancyService vacancyService

	RecruiterController(RecruiterService recruiterService, VacancyService vacancyService) {
		this.recruiterService = recruiterService
		this.vacancyService = vacancyService
	}

	RecruiterResponse create(int enterpriseId, RecruiterRequest request) {
		RecruiterMapper.toResponse(recruiterService.create(enterpriseId, RecruiterMapper.toEntity(request)))
	}

	RecruiterResponse updateOwnProfile(int recruiterId, String description, String password) {
		RecruiterMapper.toResponse(recruiterService.updateOwnProfile(recruiterId, description, password))
	}

	void removeByEnterprise(int enterpriseId, int recruiterId) {
		recruiterService.removeByEnterprise(enterpriseId, recruiterId)
	}

	List<RecruiterResponse> listByEnterprise(int enterpriseId) {
		recruiterService.listByEnterprise(enterpriseId).collect { RecruiterMapper.toResponse(it) }
	}

	/** Todas as vagas da empresa do recrutador, em qualquer status. 404 se o recrutador não existir. */
	List<VacancyAnonymousResponse> listEnterpriseVacancies(int recruiterId) {
		int enterpriseId = recruiterService.enterpriseIdOf(recruiterId)
		VacancyMapper.toAnonymousResponses(vacancyService.findByEnterprise(enterpriseId))
	}
}
