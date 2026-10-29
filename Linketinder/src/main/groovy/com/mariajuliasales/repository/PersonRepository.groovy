package com.mariajuliasales.repository

interface PersonRepository<T> extends Repository<T> {

	T findByEmail(String email)
}
