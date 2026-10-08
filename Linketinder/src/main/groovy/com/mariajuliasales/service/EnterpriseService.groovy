package com.mariajuliasales.service

import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.util.ValidateUtil

class EnterpriseService {

    private final EnterpriseDAO enterpriseDAO

    EnterpriseService(EnterpriseDAO enterpriseDAO) {
        this.enterpriseDAO = enterpriseDAO
    }

    Enterprise create(Enterprise enterprise) {
        validate(enterprise, true)
        return enterpriseDAO.create(enterprise)
    }

    Enterprise getEnterpriseById(int id) {
        enterpriseDAO.findById(id)
    }

    List<Enterprise> getAllEnterprises() {
        enterpriseDAO.findAll()
    }

    Enterprise update(Enterprise enterprise) {
        validate(enterprise, false)
        Enterprise updated = enterpriseDAO.update(enterprise)
        if (!updated) {
            throw new IllegalArgumentException("Enterprise not found")
        }
        updated
    }

    void delete(int id) {
        if (!enterpriseDAO.delete(id)) {
            throw new IllegalArgumentException("Enterprise not found")
        }
    }

    private static void validate(Enterprise enterprise, boolean passwordRequired) {
        if (enterprise == null) {
            throw new IllegalArgumentException("Invalid enterprise data")
        }

        if (!ValidateUtil.isValidCnpj(enterprise.cnpj)) {
            throw new IllegalArgumentException("Invalid enterprise cnpj")
        }

        if (!ValidateUtil.isValidEmail(enterprise.email)) {
            throw new IllegalArgumentException("Invalid enterprise email")
        }

        if ((passwordRequired || enterprise.password) && !ValidateUtil.isValidPassword(enterprise.password)) {
            throw new IllegalArgumentException("Invalid enterprise password: it must have at least 6 characters")
        }

        if (!enterprise.name?.trim()) {
            throw new IllegalArgumentException("Invalid enterprise name")
        }

        if (enterprise.address == null) {
            throw new IllegalArgumentException("Invalid enterprise address")
        }

        if (!ValidateUtil.isValidCep(enterprise.address.cep)) {
            throw new IllegalArgumentException("Invalid enterprise cep")
        }

        if (!ValidateUtil.isValidLocation(enterprise.address)) {
            throw new IllegalArgumentException("Invalid enterprise city or state")
        }
    }

}