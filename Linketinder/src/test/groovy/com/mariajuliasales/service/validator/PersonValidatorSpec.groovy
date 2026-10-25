package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import spock.lang.Specification
import spock.lang.Unroll

import static com.mariajuliasales.fixture.TestData.validAddress
import static com.mariajuliasales.fixture.TestData.validCandidate

class PersonValidatorSpec extends Specification {

	def "valid person should pass on create and on update"() {
		when:
		PersonValidator.validateForCreate(validCandidate())
		PersonValidator.validateForUpdate(validCandidate())

		then:
		noExceptionThrown()
	}

	def "null person should fail"() {
		when:
		PersonValidator.validateForCreate(null)

		then:
		ValidationException exception = thrown()
		exception.message == "Dados não informados"
	}

	@Unroll
	def "person with invalid #field '#value' should fail with '#message'"() {
		when:
		PersonValidator.validateForCreate(validCandidate([(field): value]))

		then:
		ValidationException exception = thrown()
		exception.message == message

		where:
		field     | value        || message
		"name"    | null         || "Nome é obrigatório"
		"name"    | "   "        || "Nome é obrigatório"
		"email"   | "joao@email" || "E-mail inválido"
		"email"   | null         || "E-mail inválido"
		"address" | null         || "Endereço é obrigatório"
	}

	@Unroll
	def "person with invalid address #field '#value' should fail with '#message'"() {
		when:
		PersonValidator.validateForCreate(validCandidate(address: validAddress([(field): value])))

		then:
		ValidationException exception = thrown()
		exception.message == message

		where:
		field   | value || message
		"cep"   | "123" || "CEP inválido"
		"cep"   | null  || "CEP inválido"
		"city"  | " "   || "Cidade e UF são obrigatórias"
		"state" | "São" || "Cidade e UF são obrigatórias"
	}

	@Unroll
	def "password '#password' should be required on create"() {
		when:
		PersonValidator.validateForCreate(validCandidate(password: password))

		then:
		ValidationException exception = thrown()
		exception.message == "A senha deve ter pelo menos 6 caracteres"

		where:
		password << [null, "", "12345"]
	}

	@Unroll
	def "empty password '#password' should be accepted on update"() {
		when:
		PersonValidator.validateForUpdate(validCandidate(password: password))

		then:
		noExceptionThrown()

		where:
		password << [null, ""]
	}

	def "short password should fail on update"() {
		when:
		PersonValidator.validateForUpdate(validCandidate(password: "12345"))

		then:
		ValidationException exception = thrown()
		exception.message == "A senha deve ter pelo menos 6 caracteres"
	}
}
