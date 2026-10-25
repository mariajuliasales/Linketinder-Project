package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.dto.response.CandidateResponse
import com.mariajuliasales.model.Candidate

final class CandidateMapper {

	static Candidate toEntity(CandidateRequest request) {
		if (request == null) {
			return null
		}
		new Candidate(
				name: request.name(),
				email: request.email(),
				password: request.password(),
				address: AddressMapper.toEntity(request.address()),
				description: request.description(),
				competences: CompetenceMapper.toEntities(request.competences()),
				cpf: request.cpf(),
				birthDate: request.birthDate(),
				training: request.training()
				)
	}

	static CandidateResponse toResponse(Candidate candidate) {
		if (candidate == null) {
			return null
		}
		new CandidateResponse(
				candidate.id,
				candidate.name,
				candidate.email,
				AddressMapper.toResponse(candidate.address),
				candidate.description,
				CompetenceMapper.toNames(candidate.competences),
				candidate.cpf,
				candidate.age,
				candidate.training
				)
	}

	static CandidateAnonymousResponse toAnonymousResponse(Candidate candidate) {
		if (candidate == null) {
			return null
		}
		new CandidateAnonymousResponse(
				candidate.id,
				candidate.training,
				candidate.description,
				CompetenceMapper.toNames(candidate.competences)
				)
	}
}
