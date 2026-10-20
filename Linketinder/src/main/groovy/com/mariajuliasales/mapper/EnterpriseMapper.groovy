package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.EnterpriseRequest
import com.mariajuliasales.dto.response.EnterpriseAnonymousResponse
import com.mariajuliasales.dto.response.EnterpriseResponse
import com.mariajuliasales.model.Enterprise

final class EnterpriseMapper {

	static Enterprise toEntity(EnterpriseRequest request) {
		if (request == null) {
			return null
		}
		new Enterprise(
				name: request.name(),
				email: request.email(),
				password: request.password(),
				address: AddressMapper.toEntity(request.address()),
				description: request.description(),
				competences: CompetenceMapper.toEntities(request.competences()),
				cnpj: request.cnpj()
				)
	}

	static EnterpriseResponse toResponse(Enterprise enterprise) {
		if (enterprise == null) {
			return null
		}
		new EnterpriseResponse(
				enterprise.id,
				enterprise.name,
				enterprise.email,
				AddressMapper.toResponse(enterprise.address),
				enterprise.description,
				CompetenceMapper.toNames(enterprise.competences),
				enterprise.cnpj,
				VacancyMapper.toAnonymousResponses(enterprise.vacancies)
				)
	}

	static EnterpriseAnonymousResponse toAnonymousResponse(Enterprise enterprise) {
		if (enterprise == null) {
			return null
		}
		new EnterpriseAnonymousResponse(
				enterprise.id,
				enterprise.address?.state,
				enterprise.address?.country,
				enterprise.description,
				CompetenceMapper.toNames(enterprise.competences),
				enterprise.vacancies.size()
				)
	}
}
