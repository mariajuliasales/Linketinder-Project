package com.mariajuliasales.controller

import com.mariajuliasales.dto.response.CompetenceResponse

interface CompetenceCatalog {

	List<CompetenceResponse> findAll()
}
