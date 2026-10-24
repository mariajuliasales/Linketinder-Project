package com.mariajuliasales.dao

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityInUseException
import org.postgresql.util.PSQLException
import org.postgresql.util.ServerErrorMessage
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.SQLException

class SqlExceptionTranslatorSpec extends Specification {

	private static PSQLException postgresError(String sqlState, String constraint) {
		new PSQLException(new ServerErrorMessage("SERROR\u0000C${sqlState}\u0000Mviolation\u0000n${constraint}\u0000"))
	}

	@Unroll
	def "unique violation on '#constraint' should become a duplicate error with message '#message'"() {
		when:
		Exception translated = SqlExceptionTranslator.translate(postgresError('23505', constraint))

		then:
		translated instanceof DuplicateEntityException
		translated.message == message
		translated.cause instanceof PSQLException

		where:
		constraint            || message
		'person_email_key'    || 'E-mail já cadastrado'
		'candidate_cpf_key'   || 'CPF já cadastrado'
		'enterprise_cnpj_key' || 'CNPJ já cadastrado'
		'competence_name_key' || 'Competência já cadastrada'
		'unknown_key'         || 'Registro já cadastrado'
	}

	@Unroll
	def "sql state #sqlState should become an in use error"() {
		when:
		Exception translated = SqlExceptionTranslator.translate(new SQLException('violation', sqlState))

		then:
		translated instanceof EntityInUseException
		translated.message == 'Registro em uso por outros cadastros'

		where:
		sqlState << ['23503', '23001']
	}

	def "other sql errors should be kept as they are"() {
		given:
		SQLException connectionError = new SQLException('connection refused', '08001')

		expect:
		SqlExceptionTranslator.translate(connectionError).is(connectionError)
	}
}
