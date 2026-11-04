package com.mariajuliasales

import com.mariajuliasales.controller.CandidateController
import com.mariajuliasales.controller.CompetenceController
import com.mariajuliasales.controller.Controllers
import com.mariajuliasales.controller.EnterpriseController
import com.mariajuliasales.controller.VacancyController
import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.dao.CompetenceLinkDAO
import com.mariajuliasales.dao.ConnectionFactories
import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.dao.DatabaseConfig
import com.mariajuliasales.dao.DatabaseConnection
import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.dao.VacancyDAO
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.CompetenceService
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService
import com.mariajuliasales.service.validator.CandidateValidator
import com.mariajuliasales.service.validator.CompetenceValidator
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import com.mariajuliasales.service.validator.VacancyValidator

final class Application {

	static DatabaseConnection connectToDatabase() {
		DatabaseConfig config = DatabaseConfig.fromEnvironment()
		DatabaseConnection database = new DatabaseConnection(ConnectionFactories.forConfig(config))
		database.withConnection { println "Conectado ao banco: ${config.url}" }
		database
	}

	static Controllers createControllers(ConnectionManager database) {
		CompetenceDAO competenceDAO = new CompetenceDAO(database)
		CompetenceLinkDAO competenceLinkDAO = new CompetenceLinkDAO(competenceDAO)
		VacancyDAO vacancyDAO = new VacancyDAO(database, competenceLinkDAO)

		PersonValidator personValidator = new PersonValidator()
		CandidateService candidateService = new CandidateService(
				new CandidateDAO(database, competenceLinkDAO), new CandidateValidator(personValidator))
		EnterpriseService enterpriseService = new EnterpriseService(
				new EnterpriseDAO(database, competenceLinkDAO, vacancyDAO), new EnterpriseValidator(personValidator))
		VacancyService vacancyService = new VacancyService(vacancyDAO, new VacancyValidator())
		CompetenceService competenceService = new CompetenceService(competenceDAO, new CompetenceValidator())

		new Controllers(
				new CandidateController(candidateService),
				new EnterpriseController(enterpriseService),
				new VacancyController(vacancyService, enterpriseService),
				new CompetenceController(competenceService)
				)
	}
}
