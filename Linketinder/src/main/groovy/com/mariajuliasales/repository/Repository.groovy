package com.mariajuliasales.repository

interface Repository<T> {

	T create(T entity)

	List<T> findAll()

	T findById(int id)

	T update(T entity)

	boolean delete(int id)
}
