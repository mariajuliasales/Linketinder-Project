package com.mariajuliasales.controller

import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.dto.response.CandidateResponse
import com.mariajuliasales.mapper.CandidateMapper
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.service.CandidateService

class CandidateController {

	private final CandidateService candidateService

	CandidateController(CandidateService candidateService) {
		this.candidateService = candidateService
	}

	List<CandidateAnonymousResponse> findAll() {
		candidateService.findAll().collect { Candidate candidate -> CandidateMapper.toAnonymousResponse(candidate) }
	}

	CandidateResponse findById(int id) {
		CandidateMapper.toResponse(candidateService.findById(id))
	}

	CandidateResponse create(CandidateRequest request) {
		CandidateMapper.toResponse(candidateService.create(CandidateMapper.toEntity(request)))
	}

	CandidateResponse update(int id, CandidateRequest request) {
		Candidate candidate = CandidateMapper.toEntity(request)
		candidate?.id = id
		CandidateMapper.toResponse(candidateService.update(candidate))
	}

	void delete(int id) {
		candidateService.delete(id)
	}
}
