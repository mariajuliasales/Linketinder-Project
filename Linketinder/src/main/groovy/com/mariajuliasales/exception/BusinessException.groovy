package com.mariajuliasales.exception

abstract class BusinessException extends RuntimeException {

	BusinessException(String message) {
		super(message)
	}

	BusinessException(String message, Throwable cause) {
		super(message, cause)
	}
}
