package com.mariajuliasales.service

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence
import com.mariajuliasales.repository.Database
import com.mariajuliasales.util.ValidateUtil

class CandidateService {

    private final Database database

    CandidateService(Database database) {
        this.database = database
    }

    Candidate create(int id, String name, String email, String state, String cep, String description, List<Competence> competences, String cpf, int age) {
        if (ValidateUtil.isValidCpf(cpf) && ValidateUtil.isValidEmail(email)) {
            Candidate candidate = new Candidate(id, name, email, state, cep, description, competences, cpf, age)
            database.createCandidate(candidate)
        } else {
            throw new IllegalArgumentException("Invalid candidate data")
        }
    }

    List<Candidate> getAllCandidates() {
        database.getCandidates().each {it::viewProfileAnonymous()
        }
    }

    Candidate getCandidateById(int id) {
        database.findCandidateById(id)
    }

}
