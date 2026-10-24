package com.mariajuliasales.fixture

import com.mariajuliasales.model.Address
import com.mariajuliasales.model.Candidate
import com.mariajuliasales.model.Competence
import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy

import java.time.LocalDate

final class TestData {

	static Address validAddress(Map changes = [:]) {
		new Address([cep: "01310-000", street: "Avenida Paulista", number: "1000", city: "São Paulo", state: "SP"] + changes)
	}

	static Candidate validCandidate(Map changes = [:]) {
		new Candidate([id: 1, name: "João Silva", email: "joao@email.com", password: "123456",
			address: validAddress(), description: "Desenvolvedor Java",
			competences: [new Competence("Java")], cpf: "529.982.247-25",
			birthDate: LocalDate.of(1995, 5, 20), training: "Ciência da Computação"] + changes)
	}

	static Enterprise validEnterprise(Map changes = [:]) {
		new Enterprise([id: 1, name: "TechSolutions Brasil", email: "contato@techsolutions.com.br", password: "123456",
			address: validAddress(), description: "Desenvolvimento de software sob medida",
			competences: [new Competence("Java")], cnpj: "11.222.333/0001-81"] + changes)
	}

	static Vacancy validVacancy(Map changes = [:]) {
		new Vacancy([id: 1, title: "Desenvolvedor Groovy", description: "Desenvolvimento de APIs",
			competences: [new Competence("Groovy")], address: validAddress(),
			enterprise: validEnterprise()] + changes)
	}
}
