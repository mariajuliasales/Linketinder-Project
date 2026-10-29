package com.mariajuliasales.menu

import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.mapper.AddressMapper
import com.mariajuliasales.mapper.CandidateMapper
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.service.CandidateService

class CandidateMenu extends Menu {

	private final CandidateService candidateService
	private final AddressForm addressForm
	private final CompetenceForm competenceForm

	CandidateMenu(ConsoleInput input, ConsoleView view, CandidateService candidateService, AddressForm addressForm,
	CompetenceForm competenceForm) {
		super('Candidatos', input, view)
		this.candidateService = candidateService
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
		List<String> profiles = candidateService.findAll().collect { Candidate candidate ->
			ResponseFormatter.formatAnonymousCandidate(CandidateMapper.toAnonymousResponse(candidate))
		}
		view.showAll(profiles, 'Nenhum candidato cadastrado.')
	}

	private void showCandidate() {
		view.showMessage(ResponseFormatter.formatCandidate(CandidateMapper.toResponse(findCandidate())))
	}

	private void createCandidate() {
		view.showMessage('Criando novo candidato...')
		CandidateRequest request = new CandidateRequest(
				input.readText('Digite o nome do candidato:'),
				input.readText('Digite o email do candidato:'),
				input.readText('Digite a senha (mínimo 6 caracteres):'),
				addressForm.read('Endereço do candidato:', null),
				input.readText('Digite a descrição do candidato:'),
				competenceForm.read('Digite as competências do candidato (separadas por vírgula):', [])*.name,
				input.readText('Digite o CPF do candidato:'),
				input.readDate('Digite a data de nascimento (dd/mm/aaaa):', null),
				input.readText('Digite a formação do candidato:')
				)
		Candidate candidate = candidateService.create(CandidateMapper.toEntity(request))
		view.showMessage("Candidato criado com sucesso! ID: ${candidate.id}")
	}

	private void updateCandidate() {
		Candidate candidate = findCandidate()
		view.showMessage('Atualizando candidato (Enter mantém o valor atual)...')
		candidate.name = input.readText('Nome', candidate.name)
		candidate.email = input.readText('Email', candidate.email)
		candidate.password = input.readText('Nova senha (Enter mantém a atual):')
		candidate.address = AddressMapper.toEntity(addressForm.read('Endereço:', candidate.address))
		candidate.description = input.readText('Descrição', candidate.description)
		candidate.cpf = input.readText('CPF', candidate.cpf)
		candidate.birthDate = input.readDate('Data de nascimento', candidate.birthDate)
		candidate.training = input.readText('Formação', candidate.training)
		candidate.competences = competenceForm.read('Competências', candidate.competences)

		candidateService.update(candidate)
		view.showMessage('Candidato atualizado com sucesso!')
	}

	private void deleteCandidate() {
		candidateService.delete(input.readInt('Digite o ID do candidato a excluir:'))
		view.showMessage('Candidato excluído com sucesso!')
	}

	private Candidate findCandidate() {
		candidateService.findById(input.readInt('Digite o ID do candidato:'))
	}
}
