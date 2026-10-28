package com.mariajuliasales.service.validator

interface Validator<T> {

	void validateForCreate(T entity)

	void validateForUpdate(T entity)
}
