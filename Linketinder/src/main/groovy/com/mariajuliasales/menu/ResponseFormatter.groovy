package com.mariajuliasales.menu

import com.mariajuliasales.dto.response.AddressResponse
import com.mariajuliasales.dto.response.CandidateAnonymousResponse
import com.mariajuliasales.dto.response.CandidateResponse
import com.mariajuliasales.dto.response.EnterpriseAnonymousResponse
import com.mariajuliasales.dto.response.EnterpriseResponse
import com.mariajuliasales.dto.response.VacancyAnonymousResponse

final class ResponseFormatter {

	static String formatCandidate(CandidateResponse candidate) {
		"Perfil do Candidato ${candidate.id()}: Nome: ${candidate.name()} | Email: ${candidate.email()} | " +
				"CPF: ${candidate.cpf()} | Idade: ${candidate.age()} | Formação: ${candidate.training()} | " +
				"Endereço: ${formatAddress(candidate.address())} | Descrição pessoal: ${candidate.description()} | " +
				"Competências: ${candidate.competences().join(', ')}"
	}

	static String formatAnonymousCandidate(CandidateAnonymousResponse candidate) {
		"Perfil do Candidato ${candidate.id()}: Formação: ${candidate.training()} | " +
				"Descrição pessoal: ${candidate.description()} | Competências: ${candidate.competences().join(', ')}"
	}

	static String formatEnterprise(EnterpriseResponse enterprise) {
		String profile = "Empresa ${enterprise.id()}: ${enterprise.name()} | CNPJ: ${enterprise.cnpj()} | " +
				"Email: ${enterprise.email()} | Endereço: ${formatAddress(enterprise.address())} | " +
				"Descrição: ${enterprise.description()} | Competências: ${enterprise.competences().join(', ')}"
		List<String> vacancyLines = enterprise.vacancies().collect { "  ${formatVacancy(it)}".toString() }
		([
			profile,
			"Vagas (${enterprise.vacancies().size()}):".toString()
		] + vacancyLines).join('\n')
	}

	static String formatAnonymousEnterprise(EnterpriseAnonymousResponse enterprise) {
		"Perfil da Empresa ${enterprise.id()}: Estado: ${enterprise.state()} | País: ${enterprise.country()} | " +
				"Descrição da empresa: ${enterprise.description()} | " +
				"Competências: ${enterprise.competences().join(', ')} | Vagas: ${enterprise.vacancyCount()}"
	}

	static String formatVacancy(VacancyAnonymousResponse vacancy) {
		"Vaga: ${vacancy.id()}, Título: ${vacancy.title()}, Descrição: ${vacancy.description()}, " +
				"Local: ${vacancy.city()}/${vacancy.state()}, Status: ${vacancy.status()}, " +
				"Competências: ${vacancy.competences().join(', ')}"
	}

	static String formatAddress(AddressResponse address) {
		if (address == null) {
			return ''
		}
		String streetLine = [
			[
				address.street(),
				address.number()
			].findAll().join(', '),
			address.complement(),
			address.neighborhood()
		].findAll().join(' - ')
		String cityLine = "${address.city()}/${address.state()} - ${address.country()}"
		String cepLine = address.cep() ? "CEP ${address.cep()}" : null
		[streetLine, cityLine, cepLine].findAll().join(', ')
	}
}
