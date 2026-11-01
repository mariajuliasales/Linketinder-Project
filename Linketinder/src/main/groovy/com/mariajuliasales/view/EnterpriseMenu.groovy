package com.mariajuliasales.view

import com.mariajuliasales.controller.EnterpriseController
import com.mariajuliasales.dto.request.EnterpriseRequest
import com.mariajuliasales.dto.response.EnterpriseAnonymousResponse
import com.mariajuliasales.dto.response.EnterpriseResponse

class EnterpriseMenu extends Menu {

	private final EnterpriseController enterpriseController
	private final AddressForm addressForm
	private final CompetenceForm competenceForm

	EnterpriseMenu(ConsoleInput input, ConsoleView view, EnterpriseController enterpriseController, AddressForm addressForm,
	CompetenceForm competenceForm) {
		super('Empresas', input, view)
		this.enterpriseController = enterpriseController
		this.addressForm = addressForm
		this.competenceForm = competenceForm
	}

	@Override
	protected List<MenuOption> getOptions() {
		[
			new MenuOption('Listar empresas', this.&listEnterprises),
			new MenuOption('Ver dados completos de uma empresa', this.&showEnterprise),
			new MenuOption('Cadastrar empresa', this.&createEnterprise),
			new MenuOption('Atualizar empresa', this.&updateEnterprise),
			new MenuOption('Excluir empresa (e suas vagas)', this.&deleteEnterprise)
		]
	}

	private void listEnterprises() {
		List<String> profiles = enterpriseController.findAll().collect { EnterpriseAnonymousResponse enterprise ->
			ResponseFormatter.formatAnonymousEnterprise(enterprise)
		}
		view.showAll(profiles, 'Nenhuma empresa cadastrada.')
	}

	private void showEnterprise() {
		view.showMessage(ResponseFormatter.formatEnterprise(findEnterprise()))
	}

	private void createEnterprise() {
		view.showMessage('Criando nova empresa...')
		EnterpriseRequest request = new EnterpriseRequest(
				input.readText('Digite o nome da empresa:'),
				input.readText('Digite o email corporativo da empresa:'),
				input.readText('Digite a senha (mínimo 6 caracteres):'),
				addressForm.read('Endereço da empresa:', null),
				input.readText('Digite a descrição da empresa:'),
				competenceForm.read('Digite as competências procuradas (separadas por vírgula):', []),
				input.readText('Digite o CNPJ da empresa:')
				)
		EnterpriseResponse enterprise = enterpriseController.create(request)
		view.showMessage("Empresa cadastrada com sucesso! ID: ${enterprise.id()}")
	}

	private void updateEnterprise() {
		EnterpriseResponse current = findEnterprise()
		view.showMessage('Atualizando empresa (Enter mantém o valor atual)...')
		String name = input.readText('Nome', current.name())
		String email = input.readText('Email', current.email())
		String password = input.readText('Nova senha (Enter mantém a atual):')
		String cnpj = input.readText('CNPJ', current.cnpj())
		EnterpriseRequest request = new EnterpriseRequest(
				name,
				email,
				password,
				addressForm.read('Endereço:', current.address()),
				input.readText('Descrição', current.description()),
				competenceForm.read('Competências', current.competences()),
				cnpj
				)
		enterpriseController.update(current.id(), request)
		view.showMessage('Empresa atualizada com sucesso!')
	}

	private void deleteEnterprise() {
		enterpriseController.delete(input.readInt('Digite o ID da empresa a excluir:'))
		view.showMessage('Empresa excluída com sucesso!')
	}

	private EnterpriseResponse findEnterprise() {
		enterpriseController.findById(input.readInt('Digite o ID da empresa:'))
	}
}
