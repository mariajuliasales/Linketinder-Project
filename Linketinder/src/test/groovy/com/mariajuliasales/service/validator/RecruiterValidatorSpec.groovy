package com.mariajuliasales.service.validator

import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.RecruitmentArea
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate

import static com.mariajuliasales.fixture.TestData.validRecruiter

class RecruiterValidatorSpec extends Specification {

	RecruiterValidator validator = new RecruiterValidator()

	def "valid recruiter should pass on create and update"() {
		when:
		validator.validateForCreate(validRecruiter())
		validator.validateForUpdate(validRecruiter())

		then:
		noExceptionThrown()
	}

	def "null recruiter should fail"() {
		when:
		validator.validateForCreate(null)

		then:
		ValidationException exception = thrown()
		exception.message == "Dados não informados"
	}

	def "recruiter without address or competences should pass"() {
		expect:
		validRecruiter().address == null
		validRecruiter().competences.isEmpty()

		when:
		validator.validateForCreate(validRecruiter())

		then:
		noExceptionThrown()
	}

	@Unroll
	def "recruiter with invalid #field should fail with '#message'"() {
		when:
		validator.validateForCreate(validRecruiter([(field): value]))

		then:
		ValidationException exception = thrown()
		exception.message == message

		where:
		field       | value             || message
		"name"      | null              || "Nome é obrigatório"
		"name"      | "  "              || "Nome é obrigatório"
		"email"     | null              || "E-mail inválido"
		"email"     | "invalid"         || "E-mail inválido"
		"password"  | null              || "A senha deve ter pelo menos 6 caracteres"
		"password"  | ""                || "A senha deve ter pelo menos 6 caracteres"
		"password"  | "12345"           || "A senha deve ter pelo menos 6 caracteres"
		"birthDate" | null              || "Data de nascimento é obrigatória"
		"area"      | null              || "Área de recrutamento inválida"
	}

	@Unroll
	def "recruiter with invalid cpf '#cpf' should fail"() {
		when:
		validator.validateForCreate(validRecruiter(cpf: cpf))

		then:
		ValidationException exception = thrown()
		exception.message == "CPF inválido"

		where:
		cpf << [null, "", "invalid", "123.456.789-00", "000.000.000-00"]
	}

	@Unroll
	def "recruiter with valid cpf '#cpf' should pass, with or without mask"() {
		when:
		validator.validateForCreate(validRecruiter(cpf: cpf))

		then:
		noExceptionThrown()

		where:
		cpf << ["301.958.476-01", "30195847601"]
	}

	def "recruiter born in the future should fail"() {
		when:
		validator.validateForCreate(validRecruiter(birthDate: LocalDate.now().plusDays(1)))

		then:
		ValidationException exception = thrown()
		exception.message == "Data de nascimento não pode estar no futuro"
	}

	def "recruiter who turns 18 today should pass"() {
		when:
		validator.validateForCreate(validRecruiter(birthDate: LocalDate.now().minusYears(18)))

		then:
		noExceptionThrown()
	}

	@Unroll
	def "recruiter born on #birthDate (under 18) should fail"() {
		when:
		validator.validateForCreate(validRecruiter(birthDate: birthDate))

		then:
		ValidationException exception = thrown()
		exception.message == "O recrutador deve ter pelo menos 18 anos"

		where:
		birthDate << [LocalDate.now().minusYears(18).plusDays(1), LocalDate.now().minusYears(10), LocalDate.now()]
	}

	@Unroll
	def "area '#name' from the list should pass, ignoring case"() {
		when:
		validator.validateForCreate(validRecruiter(area: RecruitmentArea.fromName(name)))

		then:
		noExceptionThrown()

		where:
		name << ["ti", "TI", "segurança da informação"]
	}

	@Unroll
	def "area '#name' outside the list or empty should fail with 'Área de recrutamento inválida'"() {
		when:
		validator.validateForCreate(validRecruiter(area: RecruitmentArea.fromName(name)))

		then:
		ValidationException exception = thrown()
		exception.message == "Área de recrutamento inválida"

		where:
		name << ["Jurídico", "", "  ", null]
	}

	@Unroll
	def "update should accept an empty password '#password' and keep the other rules"() {
		when:
		validator.validateForUpdate(validRecruiter(password: password))

		then:
		noExceptionThrown()

		where:
		password << [null, ""]
	}

	def "update with a short password should fail"() {
		when:
		validator.validateForUpdate(validRecruiter(password: "12345"))

		then:
		ValidationException exception = thrown()
		exception.message == "A senha deve ter pelo menos 6 caracteres"
	}

	def "update should still check the recruiter fields"() {
		when:
		validator.validateForUpdate(validRecruiter(password: null, cpf: "invalid"))

		then:
		ValidationException exception = thrown()
		exception.message == "CPF inválido"
	}

	def "error messages should not repeat the received cpf or email"() {
		when:
		validator.validateForCreate(validRecruiter(cpf: "123.456.789-00"))

		then:
		ValidationException cpfError = thrown()
		!cpfError.message.contains("123")

		when:
		validator.validateForCreate(validRecruiter(email: "not-an-email"))

		then:
		ValidationException emailError = thrown()
		!emailError.message.contains("not-an-email")
	}
}
