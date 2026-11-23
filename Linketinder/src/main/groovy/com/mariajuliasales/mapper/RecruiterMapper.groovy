package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.RecruiterRequest
import com.mariajuliasales.dto.response.RecruiterResponse
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.model.RecruitmentArea

final class RecruiterMapper {

	/** O enterpriseId não vem no request: o RecruiterService o define a partir da empresa. */
	static Recruiter toEntity(RecruiterRequest request) {
		if (request == null) {
			return null
		}
		new Recruiter(
				name: request.name(),
				email: request.email(),
				password: request.password(),
				description: request.description(),
				cpf: request.cpf(),
				birthDate: request.birthDate(),
				area: RecruitmentArea.fromName(request.area())
				)
	}

	static RecruiterResponse toResponse(Recruiter recruiter) {
		if (recruiter == null) {
			return null
		}
		new RecruiterResponse(
				recruiter.id,
				recruiter.name,
				recruiter.email,
				recruiter.area?.displayName,
				recruiter.description
				)
	}
}
