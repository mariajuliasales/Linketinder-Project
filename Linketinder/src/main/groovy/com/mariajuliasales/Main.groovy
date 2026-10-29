package com.mariajuliasales

import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.dao.CompetenceLinkDAO
import com.mariajuliasales.dao.DatabaseConfig
import com.mariajuliasales.dao.DatabaseConnection
import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.dao.VacancyDAO
import com.mariajuliasales.menu.AddressForm
import com.mariajuliasales.menu.CandidateMenu
import com.mariajuliasales.menu.CompetenceForm
import com.mariajuliasales.menu.CompetenceMenu
import com.mariajuliasales.menu.ConsoleInput
import com.mariajuliasales.menu.ConsoleView
import com.mariajuliasales.menu.EndOfInputException
import com.mariajuliasales.menu.EnterpriseMenu
import com.mariajuliasales.menu.MainMenu
import com.mariajuliasales.menu.VacancyMenu
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.CompetenceService
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService
import com.mariajuliasales.service.validator.CandidateValidator
import com.mariajuliasales.service.validator.CompetenceValidator
import com.mariajuliasales.service.validator.EnterpriseValidator
import com.mariajuliasales.service.validator.PersonValidator
import com.mariajuliasales.service.validator.VacancyValidator

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
	DatabaseConnection database = new DatabaseConnection(DatabaseConfig.fromEnvironment())
	database.withConnection { println "Conectado ao banco: ${database.config.url}" }
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

	ConsoleInput input = new ConsoleInput()
	ConsoleView view = new ConsoleView()
	AddressForm addressForm = new AddressForm(input, view)
	CompetenceForm competenceForm = new CompetenceForm(input, view, competenceService)

	new MainMenu(input, view, [
		new CandidateMenu(input, view, candidateService, addressForm, competenceForm),
		new EnterpriseMenu(input, view, enterpriseService, addressForm, competenceForm),
		new VacancyMenu(input, view, vacancyService, enterpriseService, competenceForm),
		new CompetenceMenu(input, view, competenceService)
	])
}
