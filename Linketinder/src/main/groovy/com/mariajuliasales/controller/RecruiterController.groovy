package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.RecruiterRequest
import com.mariajuliasales.dto.response.RecruiterResponse
import com.mariajuliasales.mapper.RecruiterMapper
import com.mariajuliasales.service.RecruiterService

/** Devolve só RecruiterResponse (sem cpf, birthDate, age e password), nunca a entidade Recruiter. */
class RecruiterController {

	private final RecruiterService recruiterService

	RecruiterController(RecruiterService recruiterService) {
		this.recruiterService = recruiterService
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
}
