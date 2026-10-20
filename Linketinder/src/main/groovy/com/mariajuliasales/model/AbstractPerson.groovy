package com.mariajuliasales.model

abstract class AbstractPerson implements Person {

	Integer id
	String name
	String email
	String password
	String description
	Address address
	List<Competence> competences = []
}
