package com.mariajuliasales.model

interface Person {

	Integer getId()

	String getName()

	String getEmail()

	String getPassword()

	String getDescription()

	Address getAddress()

	List<Competence> getCompetences()
}
