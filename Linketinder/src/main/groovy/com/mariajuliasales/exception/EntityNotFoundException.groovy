package com.mariajuliasales.exception

class EntityNotFoundException extends BusinessException {

	EntityNotFoundException(String entityName, int id) {
		super("${entityName} com ID ${id} não existe".toString())
	}

	static <T> T requireFound(T entity, String entityName, int id) {
		if (entity == null) {
			throw new EntityNotFoundException(entityName, id)
		}
		entity
	}
}
