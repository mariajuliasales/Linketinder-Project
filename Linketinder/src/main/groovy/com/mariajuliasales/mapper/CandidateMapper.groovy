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
                name: request.name(),
                email: request.email(),
                password: request.password(),
                address: request.address() ? AddressMapper.toAddress(request.address()) : null,
                description: request.description(),
                competences: request.competences()?.collect { new Competence(it) } ?: [],
                cpf: request.cpf(),
                birthDate: request.birthDate(),
                training: request.training()
        )
    }

    static CandidateResponse toCandidateResponse(Candidate candidate) {

        if(!candidate)
            throw new IllegalArgumentException("Candidate cannot be null")

        new CandidateResponse(
                candidate.id,
                candidate.name,
                candidate.email,
                candidate.address ? AddressMapper.toAddressResponse(candidate.address) : null,
                candidate.description,
                candidate.competences?.collect { it.name } ?: [],
                candidate.cpf,
                candidate.age,
                candidate.training
        )
    }

    static CandidateAnonymousResponse toCandidateAnonymousResponse(Candidate candidate) {

        if(!candidate)
            throw new IllegalArgumentException("Candidate cannot be null")

        new CandidateAnonymousResponse(
                candidate.id,
                candidate.training,
                candidate.description,
                candidate.competences?.collect { it.name } ?: []
        )
    }
}
