package com.mariajuliasales.service

import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.repository.Repository
import com.mariajuliasales.service.validator.Validator

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound

class CrudService<T> implements EntityFinder<T> {

	protected final Repository<T> repository
	protected final Validator<T> validator
	protected final String entityName

	CrudService(Repository<T> repository, Validator<T> validator, String entityName) {
		this.repository = repository
		this.validator = validator
		this.entityName = entityName
	}

	T create(T entity) {
		validator.validateForCreate(entity)
		checkBusinessRules(entity)
		repository.create(entity)
	}

	List<T> findAll() {
		repository.findAll()
	}

	@Override
	T findById(int id) {
		requireFound(repository.findById(id), entityName, id)
	}

	T update(T entity) {
		validator.validateForUpdate(entity)
		checkBusinessRules(entity)
		requireFound(repository.update(entity), entityName, entity.id as int)
	}

	void delete(int id) {
		if (!repository.delete(id)) {
			throw new EntityNotFoundException(entityName, id)
		}
	}

	protected void checkBusinessRules(T entity) {
	}
}
