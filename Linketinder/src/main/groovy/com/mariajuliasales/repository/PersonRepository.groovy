package com.mariajuliasales.repository

import java.sql.Connection

interface PersonRepository<T> extends Repository<T> {

	T findByEmail(String email)

	/** Apaga a pessoa dentro da transação recebida. Devolve false se o id não existe. */
	boolean delete(Connection connection, int id)
}
