package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.VacancyRequest
import com.mariajuliasales.dto.response.VacancyAnonymousResponse
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus

final class VacancyMapper {

	static Vacancy toEntity(VacancyRequest request, Enterprise enterprise) {
		if (request == null) {
			return null
		}
		new Vacancy(
				title: request.title(),
				description: request.description(),
				address: new Address(city: request.city(), state: request.state()),
				status: toStatus(request.status()),
				competences: CompetenceMapper.toEntities(request.competences()),
				enterprise: enterprise
				)
	}

	static VacancyAnonymousResponse toAnonymousResponse(Vacancy vacancy) {
		if (vacancy == null) {
			return null
		}
		new VacancyAnonymousResponse(
				vacancy.id,
				vacancy.title,
				vacancy.description,
				vacancy.address?.city,
				vacancy.address?.state,
				vacancy.status?.name(),
				CompetenceMapper.toNames(vacancy.competences)
				)
	}

	static List<VacancyAnonymousResponse> toAnonymousResponses(List<Vacancy> vacancies) {
		vacancies?.collect { Vacancy vacancy -> toAnonymousResponse(vacancy) } ?: []
	}

	private static VacancyStatus toStatus(String status) {
		if (!status?.trim()) {
			return VacancyStatus.OPEN
		}
		VacancyStatus found = VacancyStatus.values().find { it.name().equalsIgnoreCase(status.trim()) }
		if (found == null) {
			throw new ValidationException("Status inválido. Use: ${VacancyStatus.values().join(', ')}".toString())
		}
		found
	}
}
