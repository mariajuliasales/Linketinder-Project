package com.mariajuliasales

import com.mariajuliasales.dao.CandidateDAO
import com.mariajuliasales.dao.CompetenceDAO
import com.mariajuliasales.dao.DatabaseConnection
import com.mariajuliasales.dao.EnterpriseDAO
import com.mariajuliasales.dao.VacancyDAO
import com.mariajuliasales.menu.AddressForm
import com.mariajuliasales.menu.CandidateMenu
import com.mariajuliasales.menu.CompetenceForm
import com.mariajuliasales.menu.CompetenceMenu
import com.mariajuliasales.menu.ConsoleInput
import com.mariajuliasales.menu.EndOfInputException
import com.mariajuliasales.menu.EnterpriseMenu
import com.mariajuliasales.menu.MainMenu
import com.mariajuliasales.menu.VacancyMenu
import com.mariajuliasales.service.CandidateService
import com.mariajuliasales.service.CompetenceService
import com.mariajuliasales.service.EnterpriseService
import com.mariajuliasales.service.VacancyService

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
	DatabaseConnection database = new DatabaseConnection()
	database.withConnection { println "Conectado ao banco: ${database.url}" }
	database
}

static MainMenu buildMainMenu(DatabaseConnection database) {
	CompetenceDAO competenceDAO = new CompetenceDAO(database)
	VacancyDAO vacancyDAO = new VacancyDAO(database, competenceDAO)

	CandidateService candidateService = new CandidateService(new CandidateDAO(database, competenceDAO))
	EnterpriseService enterpriseService = new EnterpriseService(new EnterpriseDAO(database, competenceDAO, vacancyDAO))
	VacancyService vacancyService = new VacancyService(vacancyDAO)
	CompetenceService competenceService = new CompetenceService(competenceDAO)

	ConsoleInput input = new ConsoleInput()
	AddressForm addressForm = new AddressForm(input)
	CompetenceForm competenceForm = new CompetenceForm(input, competenceService)

	new MainMenu(input,
			new CandidateMenu(input, candidateService, addressForm, competenceForm),
			new EnterpriseMenu(input, enterpriseService, addressForm, competenceForm),
			new VacancyMenu(input, vacancyService, enterpriseService, competenceForm),
			new CompetenceMenu(input, competenceService))
}
