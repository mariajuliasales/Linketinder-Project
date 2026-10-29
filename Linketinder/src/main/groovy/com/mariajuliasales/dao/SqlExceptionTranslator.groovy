package com.mariajuliasales.dao

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityInUseException
import org.postgresql.util.PSQLException

import java.sql.SQLException

final class SqlExceptionTranslator {

	private static final String UNIQUE_VIOLATION = '23505'
	private static final String FOREIGN_KEY_VIOLATION = '23503'
	private static final String RESTRICT_VIOLATION = '23001'

	private static final Map<String, String> DUPLICATE_MESSAGES = [
		person_email_key    : 'E-mail já cadastrado',
		candidate_cpf_key   : 'CPF já cadastrado',
		enterprise_cnpj_key : 'CNPJ já cadastrado',
		competence_name_key : 'Competência já cadastrada'
	]

	static Exception translate(SQLException exception) {
		switch (exception.SQLState) {
			case UNIQUE_VIOLATION:
				String message = DUPLICATE_MESSAGES[constraintOf(exception)] ?: 'Registro já cadastrado'
				return new DuplicateEntityException(message, exception)
			case FOREIGN_KEY_VIOLATION:
			case RESTRICT_VIOLATION:
				return new EntityInUseException('Registro em uso por outros cadastros', exception)
			default:
				return exception
		}
	}

	private static String constraintOf(SQLException exception) {
		exception instanceof PSQLException ? exception.serverErrorMessage?.constraint : null
	}
}
