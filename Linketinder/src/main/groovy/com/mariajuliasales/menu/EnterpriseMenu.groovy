package com.mariajuliasales.menu

import com.mariajuliasales.dto.request.EnterpriseRequest
import com.mariajuliasales.mapper.AddressMapper
import com.mariajuliasales.mapper.EnterpriseMapper
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.service.EnterpriseService

class EnterpriseMenu extends Menu {

	private final EnterpriseService enterpriseService
	private final AddressForm addressForm
	private final CompetenceForm competenceForm

	EnterpriseMenu(ConsoleInput input, EnterpriseService enterpriseService, AddressForm addressForm,
	CompetenceForm competenceForm) {
		super('Empresas', input)
		this.enterpriseService = enterpriseService
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
		List<String> profiles = enterpriseService.findAll().collect { Enterprise enterprise ->
			View.formatAnonymousEnterprise(EnterpriseMapper.toAnonymousResponse(enterprise))
		}
		View.showAll(profiles, 'Nenhuma empresa cadastrada.')
	}

	private void showEnterprise() {
		View.showMessage(View.formatEnterprise(EnterpriseMapper.toResponse(findEnterprise())))
	}

	private void createEnterprise() {
		View.showMessage('Criando nova empresa...')
		EnterpriseRequest request = new EnterpriseRequest(
				input.readText('Digite o nome da empresa:'),
				input.readText('Digite o email corporativo da empresa:'),
				input.readText('Digite a senha (mínimo 6 caracteres):'),
				addressForm.read('Endereço da empresa:', null),
				input.readText('Digite a descrição da empresa:'),
				competenceForm.read('Digite as competências procuradas (separadas por vírgula):', [])*.name,
				input.readText('Digite o CNPJ da empresa:')
				)
		Enterprise enterprise = enterpriseService.create(EnterpriseMapper.toEntity(request))
		View.showMessage("Empresa cadastrada com sucesso! ID: ${enterprise.id}")
	}

	private void updateEnterprise() {
		Enterprise enterprise = findEnterprise()
		View.showMessage('Atualizando empresa (Enter mantém o valor atual)...')
		enterprise.name = input.readText('Nome', enterprise.name)
		enterprise.email = input.readText('Email', enterprise.email)
		enterprise.password = input.readText('Nova senha (Enter mantém a atual):')
		enterprise.cnpj = input.readText('CNPJ', enterprise.cnpj)
		enterprise.address = AddressMapper.toEntity(addressForm.read('Endereço:', enterprise.address))
		enterprise.description = input.readText('Descrição', enterprise.description)
		enterprise.competences = competenceForm.read('Competências', enterprise.competences)

		enterpriseService.update(enterprise)
		View.showMessage('Empresa atualizada com sucesso!')
	}

	private void deleteEnterprise() {
		enterpriseService.delete(input.readInt('Digite o ID da empresa a excluir:'))
		View.showMessage('Empresa excluída com sucesso!')
	}

	private Enterprise findEnterprise() {
		enterpriseService.findById(input.readInt('Digite o ID da empresa:'))
	}
}
