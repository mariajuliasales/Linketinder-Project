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
        if (!ValidateUtil.isValidCpf(candidate.getCpf()) || !ValidateUtil.isValidEmail(candidate.getEmail())) {
            throw new IllegalArgumentException("Invalid candidate data")
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
