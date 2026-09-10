package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.dto.response.CandidateResponse
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

    static CandidateResponse toCandidateResponse(Candidate candidate) {

        if(!candidate)
            throw new IllegalArgumentException("Candidate cannot be null")

        new CandidateResponse(
                candidate.name,
                candidate.email,
                candidate.state,
                candidate.cep,
                candidate.description,
                candidate.competences?.collect { it as Competence } ?: [],
                candidate.cpf,
                candidate.age
        )
    }

    static CandidateAnonymousResponse toCandidateAnonymousResponse(Candidate candidate) {

        if(!candidate)
            throw new IllegalArgumentException("Candidate cannot be null")

        new CandidateAnonymousResponse(
                candidate.state,
                candidate.cep,
                candidate.description,
                candidate.competences?.collect { it as Competence } ?: []
        )
    }
}