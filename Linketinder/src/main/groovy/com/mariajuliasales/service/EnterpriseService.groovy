package com.mariajuliasales.service

import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.repository.Database
import com.mariajuliasales.util.ValidateUtil

class EnterpriseService {

    private final Database database

    EnterpriseService(Database database) {
        this.database = database
    }

    Enterprise create(int id, String name, String email, String state, String cep, String description, List<Competence> competences, String cnpj, String country) {

        if (!ValidateUtil.isValidCnpj(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido: ${cnpj}")
        }

        if (!ValidateUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("E-mail inválido: ${email}")
        }

        Enterprise enterprise = new Enterprise(id, name, email, state, cep, description, competences, cnpj, country)
        return database.createEnterprise(enterprise)
    }

    Enterprise getEnterpriseById(int id) {
        database.findEnterpriseById(id)
    }

    List<Enterprise> getAllEnterprises() {
        database.getEnterprises().each {it::viewProfileAnonymous()}

    }

}
