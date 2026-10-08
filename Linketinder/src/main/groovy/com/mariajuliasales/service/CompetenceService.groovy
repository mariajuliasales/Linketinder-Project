package com.mariajuliasales.service

import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.model.Competence

class CompetenceService {

    private static final int MAX_NAME_LENGTH = 50

    private final CompetenceDAO competenceDAO

    CompetenceService(CompetenceDAO competenceDAO) {
        this.competenceDAO = competenceDAO
    }

    Competence create(String name) {
        validateName(name)
        if (competenceDAO.findByName(name)) {
            throw new IllegalArgumentException("Competence already exists")
        }
        competenceDAO.create(new Competence(name))
    }

    List<Competence> findAll() {
        competenceDAO.findAll()
    }

    Competence findById(int id) {
        Competence competence = competenceDAO.findById(id)
        if (!competence) {
            throw new IllegalArgumentException("Competence not found")
        }
        competence
    }

    Competence update(int id, String newName) {
        validateName(newName)
        Competence existing = competenceDAO.findByName(newName)
        if (existing && existing.id != id) {
            throw new IllegalArgumentException("Competence already exists")
        }
        Competence updated = competenceDAO.update(new Competence(id, newName))
        if (!updated) {
            throw new IllegalArgumentException("Competence not found")
        }
        updated
    }

    void delete(int id) {
        if (!competenceDAO.delete(id)) {
            throw new IllegalArgumentException("Competence not found")
        }
    }

    private static void validateName(String name) {
        if (!name?.trim()) {
            throw new IllegalArgumentException("Competence name cannot be empty")
        }
        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Competence name must have at most ${MAX_NAME_LENGTH} characters")
        }
    }
}
