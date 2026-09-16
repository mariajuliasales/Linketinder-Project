package com.mariajuliasales

import com.mariajuliasales.menu.Menu
import com.mariajuliasales.repository.Database
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService

static void main(String[] args) {

    // Maria Julia Sales - 2026

        try {
            Database database = new Database()
            CandidateService candidateService = new CandidateService(database)
            EnterpriseService enterpriseService = new EnterpriseService(database)
            VacancyService vacancyService = new VacancyService(database)
            Menu menu = new Menu(candidateService, enterpriseService, vacancyService)
            menu.init()
        } catch (Exception e) {
            System.err.println "Erro ao iniciar o Linketinder: ${e.message}"
            System.exit(1)
        }

}