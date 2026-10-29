package com.mariajuliasales.model

import java.time.LocalDate
import java.time.Period

class Candidate extends AbstractPerson {

	String cpf
	LocalDate birthDate
	String training

	int getAge() {
		birthDate ? Period.between(birthDate, LocalDate.now()).years : 0
	}
}
