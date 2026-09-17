package com.mariajuliasales.service

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.repository.Database
import com.mariajuliasales.util.ValidateUtil

class CandidateService {

    private final Database database

    CandidateService(Database database) {
        this.database = database
    }

    Candidate create(Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Invalid candidate data")
        }

        if (!ValidateUtil.isValidCpf(candidate.cpf)){
            throw new IllegalArgumentException("Invalid candidate cpf")
        }

        if (!ValidateUtil.isValidEmail(candidate.email)) {
            throw new IllegalArgumentException("Invalid candidate email")
        }

        database.createCandidate(candidate)
    }

    List<Candidate> getAllCandidates() {
        database.getCandidates()

    }

    Candidate getCandidateById(int id) {
        database.findCandidateById(id)
    }

}
