package com.mariajuliasales.view

import com.mariajuliasales.controller.CandidateController
import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.dto.response.CandidateResponse

class CandidateMenu extends Menu {

	private final CandidateController candidateController
	private final AddressForm addressForm
	private final CompetenceForm competenceForm

	CandidateMenu(ConsoleInput input, ConsoleView view, CandidateController candidateController, AddressForm addressForm,
	CompetenceForm competenceForm) {
		super('Candidatos', input, view)
		this.candidateController = candidateController
		this.addressForm = addressForm
		this.competenceForm = competenceForm
	}

	@Override
	protected List<MenuOption> getOptions() {
		[
			new MenuOption('Listar candidatos (perfil anônimo)', this.&listCandidates),
			new MenuOption('Ver perfil completo de um candidato', this.&showCandidate),
			new MenuOption('Cadastrar candidato', this.&createCandidate),
			new MenuOption('Atualizar candidato', this.&updateCandidate),
			new MenuOption('Excluir candidato', this.&deleteCandidate)
		]
	}

	private void listCandidates() {
		List<String> profiles = candidateController.findAll().collect { CandidateAnonymousResponse candidate ->
			ResponseFormatter.formatAnonymousCandidate(candidate)
		}
		view.showAll(profiles, 'Nenhum candidato cadastrado.')
	}

	private void showCandidate() {
		view.showMessage(ResponseFormatter.formatCandidate(findCandidate()))
	}

	private void createCandidate() {
		view.showMessage('Criando novo candidato...')
		CandidateRequest request = new CandidateRequest(
				input.readText('Digite o nome do candidato:'),
				input.readText('Digite o email do candidato:'),
				input.readText('Digite a senha (mínimo 6 caracteres):'),
				addressForm.read('Endereço do candidato:', null),
				input.readText('Digite a descrição do candidato:'),
				competenceForm.read('Digite as competências do candidato (separadas por vírgula):', []),
				input.readText('Digite o CPF do candidato:'),
				input.readDate('Digite a data de nascimento (dd/mm/aaaa):', null),
				input.readText('Digite a formação do candidato:')
				)
		CandidateResponse candidate = candidateController.create(request)
		view.showMessage("Candidato criado com sucesso! ID: ${candidate.id()}")
	}

	private void updateCandidate() {
		CandidateResponse current = findCandidate()
		view.showMessage('Atualizando candidato (Enter mantém o valor atual)...')
		CandidateRequest request = new CandidateRequest(
				input.readText('Nome', current.name()),
				input.readText('Email', current.email()),
				input.readText('Nova senha (Enter mantém a atual):'),
				addressForm.read('Endereço:', current.address()),
				input.readText('Descrição', current.description()),
				competenceForm.read('Competências', current.competences()),
				input.readText('CPF', current.cpf()),
				input.readDate('Data de nascimento', current.birthDate()),
				input.readText('Formação', current.training())
				)
		candidateController.update(current.id(), request)
		view.showMessage('Candidato atualizado com sucesso!')
	}

	private void deleteCandidate() {
		candidateController.delete(input.readInt('Digite o ID do candidato a excluir:'))
		view.showMessage('Candidato excluído com sucesso!')
	}

	private CandidateResponse findCandidate() {
		candidateController.findById(input.readInt('Digite o ID do candidato:'))
	}
}
