package com.mariajuliasales.model

class Address {

	static final String DEFAULT_COUNTRY = 'Brasil'

	Integer id
	String cep
	String street
	String number
	String complement
	String neighborhood
	String city
	String state
	String country = DEFAULT_COUNTRY
}
