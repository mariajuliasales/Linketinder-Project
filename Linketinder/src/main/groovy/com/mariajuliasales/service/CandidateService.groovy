package com.mariajuliasales.service

import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.util.ValidateUtil

class CandidateService {

    private final CandidateDAO candidateDAO

    CandidateService(CandidateDAO candidateDAO) {
        this.candidateDAO = candidateDAO
    }

    Candidate create(Candidate candidate) {
        validate(candidate, true)
        candidateDAO.create(candidate)
    }

    List<Candidate> getAllCandidates() {
        candidateDAO.findAll()
    }

    Candidate getCandidateById(int id) {
        candidateDAO.findById(id)
    }

    Candidate update(Candidate candidate) {
        validate(candidate, false)
        Candidate updated = candidateDAO.update(candidate)
        if (!updated) {
            throw new IllegalArgumentException("Candidate not found")
        }
        updated
    }

    void delete(int id) {
        if (!candidateDAO.delete(id)) {
            throw new IllegalArgumentException("Candidate not found")
        }
    }

    private static void validate(Candidate candidate, boolean passwordRequired) {
        if (candidate == null) {
            throw new IllegalArgumentException("Invalid candidate data")
        }

        if (!ValidateUtil.isValidCpf(candidate.cpf)){
            throw new IllegalArgumentException("Invalid candidate cpf")
        }

        if (!ValidateUtil.isValidEmail(candidate.email)) {
            throw new IllegalArgumentException("Invalid candidate email")
        }

        if ((passwordRequired || candidate.password) && !ValidateUtil.isValidPassword(candidate.password)) {
            throw new IllegalArgumentException("Invalid candidate password: it must have at least 6 characters")
        }

        if (!candidate.name?.trim()) {
            throw new IllegalArgumentException("Invalid candidate name")
        }

        if (candidate.address == null) {
            throw new IllegalArgumentException("Invalid candidate address")
        }

        if (!ValidateUtil.isValidCep(candidate.address.cep)) {
            throw new IllegalArgumentException("Invalid candidate cep")
        }

        if (!ValidateUtil.isValidLocation(candidate.address)) {
            throw new IllegalArgumentException("Invalid candidate city or state")
        }

        if (candidate.birthDate == null) {
            throw new IllegalArgumentException("Invalid candidate birth date")
        }

        if (!candidate.training?.trim()) {
            throw new IllegalArgumentException("Invalid candidate training")
        }
    }

}
