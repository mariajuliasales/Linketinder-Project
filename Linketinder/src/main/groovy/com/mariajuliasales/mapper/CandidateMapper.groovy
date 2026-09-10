package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence

class CandidateMapper {

    static Candidate toCandidate(CandidateRequest request) {

        if(!request)
            throw new IllegalArgumentException("CandidateRequest cannot be null")

        new Candidate(
                request.id(),
                request.name(),
                request.email(),
                request.state(),
                request.cep(),
                request.description(),
                request.competences()?.collect { it as Competence } ?: [],
                request.cpf(),
                request.age()
        )
    }
}