package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.EnterpriseRequest
import com.mariajuliasales.dto.response.EnterpriseAnonymousResponse
import com.mariajuliasales.dto.response.EnterpriseResponse
import com.mariajuliasales.mapper.EnterpriseMapper
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.service.EnterpriseService

class EnterpriseController {

	private final EnterpriseService enterpriseService

	EnterpriseController(EnterpriseService enterpriseService) {
		this.enterpriseService = enterpriseService
	}

	List<EnterpriseAnonymousResponse> findAll() {
		enterpriseService.findAll().collect { Enterprise enterprise -> EnterpriseMapper.toAnonymousResponse(enterprise) }
	}

	EnterpriseResponse findById(int id) {
		EnterpriseMapper.toResponse(enterpriseService.findById(id))
	}

	EnterpriseResponse create(EnterpriseRequest request) {
		EnterpriseMapper.toResponse(enterpriseService.create(EnterpriseMapper.toEntity(request)))
	}

	EnterpriseResponse update(int id, EnterpriseRequest request) {
		Enterprise enterprise = EnterpriseMapper.toEntity(request)
		enterprise?.id = id
		enterpriseService.update(enterprise)
		findById(id)
	}

	void delete(int id) {
		enterpriseService.delete(id)
	}
}
