package com.mariajuliasales

import com.mariajuliasales.menu.Menu
import com.mariajuliasales.repository.Database
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.EnterpriseService

static void main(String[] args) {

    // Maria Julia Sales - 2026

        try {
            Database database = new Database()
            CandidateService candidateService = new CandidateService(database)
            EnterpriseService enterpriseService = new EnterpriseService(database)
            Menu menu = new Menu(candidateService, enterpriseService)
            menu.init()
        } catch (Exception e) {
            System.err.println "Erro ao iniciar o Linketinder: ${e.message}"
            System.exit(1)
        }

}