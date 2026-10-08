package com.mariajuliasales

import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.dao.DatabaseConnection
import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.dao.VacancyDAO
import com.mariajuliasales.menu.Menu
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.CompetenceService
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService

static void main(String[] args) {

    // Maria Julia Sales - 2026

        try {
            DatabaseConnection database = new DatabaseConnection()
            database.withConnection { println "Conectado ao banco: ${database.url}" }

            CompetenceDAO competenceDAO = new CompetenceDAO(database)
            CandidateDAO candidateDAO = new CandidateDAO(database, competenceDAO)
            VacancyDAO vacancyDAO = new VacancyDAO(database, competenceDAO)
            EnterpriseDAO enterpriseDAO = new EnterpriseDAO(database, competenceDAO, vacancyDAO)

            CandidateService candidateService = new CandidateService(candidateDAO)
            EnterpriseService enterpriseService = new EnterpriseService(enterpriseDAO)
            VacancyService vacancyService = new VacancyService(vacancyDAO)
            CompetenceService competenceService = new CompetenceService(competenceDAO)

            Menu menu = new Menu(candidateService, enterpriseService, vacancyService, competenceService)
            menu.init()
        } catch (NoSuchElementException ignored) {
        } catch (Exception e) {
            System.err.println "Erro ao iniciar o Linketinder: ${e.message}"
            System.exit(1)
        }

}
