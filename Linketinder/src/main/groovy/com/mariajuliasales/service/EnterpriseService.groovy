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

    Enterprise create(Enterprise enterprise) {

        if (enterprise == null) {
            throw new IllegalArgumentException("Invalid enterprise data")
        }

        if (!ValidateUtil.isValidCnpj(enterprise.cnpj)) {
            throw new IllegalArgumentException("Invalid enterprise cnpj")
        }

        if (!ValidateUtil.isValidEmail(enterprise.email)) {
            throw new IllegalArgumentException("Invalid enterprise email")
        }

        return database.createEnterprise(enterprise)
    }

    Enterprise getEnterpriseById(int id) {
        database.findEnterpriseById(id)
    }

    List<Enterprise> getAllEnterprises() {
        database.getEnterprises().each { it::viewProfileAnonymous() }

    }

}
