package com.mariajuliasales

import com.mariajuliasales.controller.CandidateController
import com.mariajuliasales.controller.CompetenceController
import com.mariajuliasales.controller.EnterpriseController
import com.mariajuliasales.controller.VacancyController
import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.dao.CompetenceLinkDAO
import com.mariajuliasales.dao.ConnectionFactories
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
import com.mariajuliasales.view.AddressForm
import com.mariajuliasales.view.CandidateMenu
import com.mariajuliasales.view.CompetenceForm
import com.mariajuliasales.view.CompetenceMenu
import com.mariajuliasales.view.ConsoleInput
import com.mariajuliasales.view.ConsoleView
import com.mariajuliasales.view.EndOfInputException
import com.mariajuliasales.view.EnterpriseMenu
import com.mariajuliasales.view.MainMenu
import com.mariajuliasales.view.VacancyMenu

static void main(String[] args) {
	try {
		DatabaseConnection database = connectToDatabase()
		buildMainMenu(database).run()
	} catch (EndOfInputException ignored) {
		println 'Entrada encerrada. Saindo do programa...'
	} catch (Exception exception) {
		System.err.println "Erro ao executar o Linketinder: ${exception.message}"
		System.exit(1)
	}
}

static DatabaseConnection connectToDatabase() {
	DatabaseConfig config = DatabaseConfig.fromEnvironment()
	DatabaseConnection database = new DatabaseConnection(ConnectionFactories.forConfig(config))
	database.withConnection { println "Conectado ao banco: ${config.url}" }
	database
}

static MainMenu buildMainMenu(DatabaseConnection database) {
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

	CandidateController candidateController = new CandidateController(candidateService)
	EnterpriseController enterpriseController = new EnterpriseController(enterpriseService)
	VacancyController vacancyController = new VacancyController(vacancyService, enterpriseService)
	CompetenceController competenceController = new CompetenceController(competenceService)

	ConsoleInput input = new ConsoleInput()
	ConsoleView view = new ConsoleView()
	AddressForm addressForm = new AddressForm(input, view)
	CompetenceForm competenceForm = new CompetenceForm(input, view, competenceController)

	new MainMenu(input, view, [
		new CandidateMenu(input, view, candidateController, addressForm, competenceForm),
		new EnterpriseMenu(input, view, enterpriseController, addressForm, competenceForm),
		new VacancyMenu(input, view, vacancyController, enterpriseController, competenceForm),
		new CompetenceMenu(input, view, competenceController)
	])
}
