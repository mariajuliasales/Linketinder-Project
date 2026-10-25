package com.mariajuliasales.exception

class DuplicateEntityException extends BusinessException {

	DuplicateEntityException(String message) {
		super(message)
	}

	DuplicateEntityException(String message, Throwable cause) {
		super(message, cause)
	}
}
