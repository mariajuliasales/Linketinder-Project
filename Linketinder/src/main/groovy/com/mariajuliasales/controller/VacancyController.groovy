package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.VacancyRequest
import com.mariajuliasales.dto.response.VacancyAnonymousResponse
import com.mariajuliasales.mapper.VacancyMapper
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService

class VacancyController {

	private final VacancyService vacancyService
	private final EnterpriseService enterpriseService

	VacancyController(VacancyService vacancyService, EnterpriseService enterpriseService) {
		this.vacancyService = vacancyService
		this.enterpriseService = enterpriseService
	}

	List<VacancyAnonymousResponse> findAll() {
		VacancyMapper.toAnonymousResponses(vacancyService.findAll())
	}

	VacancyAnonymousResponse findById(int id) {
		VacancyMapper.toAnonymousResponse(vacancyService.findById(id))
	}

	VacancyAnonymousResponse create(int enterpriseId, VacancyRequest request) {
		Enterprise enterprise = enterpriseService.findById(enterpriseId)
		VacancyMapper.toAnonymousResponse(vacancyService.create(VacancyMapper.toEntity(request, enterprise)))
	}

	VacancyAnonymousResponse update(int id, VacancyRequest request) {
		Vacancy current = vacancyService.findById(id)
		Vacancy vacancy = VacancyMapper.toEntity(request, current.enterprise)
		vacancy?.id = id
		VacancyMapper.toAnonymousResponse(vacancyService.update(vacancy))
	}

	void delete(int id) {
		vacancyService.delete(id)
	}

	List<String> findStatuses() {
		VacancyStatus.values()*.name()
	}
}
