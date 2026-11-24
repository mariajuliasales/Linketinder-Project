package com.mariajuliasales.model

import java.time.LocalDate
import java.time.Period

/**
 * Recrutador: pessoa vinculada a uma única empresa. Não tem endereço nem competências.
 * Sem @ToString/@Canonical, para não vazar CPF ou senha em log.
 */
class Recruiter extends AbstractPerson {

	Integer enterpriseId
	RecruitmentArea area
	String cpf
	LocalDate birthDate

	@Override
	Address getAddress() {
		null
	}

	@Override
	List<Competence> getCompetences() {
		Collections.emptyList()
	}

	int getAge() {
		birthDate ? Period.between(birthDate, LocalDate.now()).years : 0
	}
}
