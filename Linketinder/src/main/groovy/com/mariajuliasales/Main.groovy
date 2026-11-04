package com.mariajuliasales

import com.mariajuliasales.controller.Controllers
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
		Controllers controllers = Application.createControllers(Application.connectToDatabase())
		buildMainMenu(controllers).run()
	} catch (EndOfInputException ignored) {
		println 'Entrada encerrada. Saindo do programa...'
	} catch (Exception exception) {
		System.err.println "Erro ao executar o Linketinder: ${exception.message}"
		System.exit(1)
	}
}

static MainMenu buildMainMenu(Controllers controllers) {
	ConsoleInput input = new ConsoleInput()
	ConsoleView view = new ConsoleView()
	AddressForm addressForm = new AddressForm(input, view)
	CompetenceForm competenceForm = new CompetenceForm(input, view, controllers.competences())

	new MainMenu(input, view, [
		new CandidateMenu(input, view, controllers.candidates(), addressForm, competenceForm),
		new EnterpriseMenu(input, view, controllers.enterprises(), addressForm, competenceForm),
		new VacancyMenu(input, view, controllers.vacancies(), controllers.enterprises(), competenceForm),
		new CompetenceMenu(input, view, controllers.competences())
	])
}
