package com.mariajuliasales.service

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.service.validator.RecruiterValidator
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validCandidate
import static com.mariajuliasales.fixture.TestData.validEnterprise
import static com.mariajuliasales.fixture.TestData.validRecruiter

class RecruiterServiceSpec extends Specification {

	RecruiterRepository recruiterRepository = Mock()
	EnterpriseRepository enterpriseRepository = Mock()
	RecruiterService recruiterService = new RecruiterService(recruiterRepository, enterpriseRepository, new RecruiterValidator())

	def "create should save a valid recruiter linked to the enterprise"() {
		given:
		Recruiter recruiter = validRecruiter(id: null, enterpriseId: null)
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)

		when:
		Recruiter result = recruiterService.create(3, recruiter)

		then:
		1 * recruiterRepository.isEmailInUse("rafael.lopes@techsolutions.com.br") >> false
		1 * recruiterRepository.create({ Recruiter saved -> saved.enterpriseId == 3 }) >> { Recruiter saved ->
			saved.id = 10
			saved
		}
		result.id == 10
		result.enterpriseId == 3
	}

	def "create should use the enterprise from the parameter, not from the recruiter data"() {
		given:
		enterpriseRepository.findById(3) >> validEnterprise(id: 3)

		when:
		recruiterService.create(3, validRecruiter(enterpriseId: 7))

		then:
		1 * recruiterRepository.create({ Recruiter saved -> saved.enterpriseId == 3 }) >> { Recruiter saved -> saved }
	}

	def "create should fail with 409 when the email is already in use, ignoring case"() {
		given:
		enterpriseRepository.findById(1) >> validEnterprise()

		when:
		recruiterService.create(1, validRecruiter(email: "Ana@X.com"))

		then:
		1 * recruiterRepository.isEmailInUse("Ana@X.com") >> true
		DuplicateEntityException exception = thrown()
		exception.message == "E-mail já cadastrado"
		0 * recruiterRepository.create(_)
	}

	def "create should accept the CPF of an existing candidate"() {
		given:
		String candidateCpf = validCandidate().cpf
		enterpriseRepository.findById(1) >> validEnterprise()
		recruiterRepository.isEmailInUse(_) >> false

		when:
		recruiterService.create(1, validRecruiter(cpf: candidateCpf))

		then:
		notThrown(Exception)
		1 * recruiterRepository.create({ Recruiter saved -> saved.cpf == candidateCpf }) >> { Recruiter saved -> saved }
	}

	def "create should fail with 404 when the enterprise does not exist"() {
		given:
		enterpriseRepository.findById(9999) >> null

		when:
		recruiterService.create(9999, validRecruiter())

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Empresa com ID 9999 não existe"
		0 * recruiterRepository.isEmailInUse(_)
		0 * recruiterRepository.create(_)
	}

	def "create should validate before checking the enterprise and the email"() {
		when:
		recruiterService.create(1, validRecruiter(name: " "))

		then:
		ValidationException exception = thrown()
		exception.message == "Nome é obrigatório"
		0 * enterpriseRepository.findById(_)
		0 * recruiterRepository.isEmailInUse(_)
		0 * recruiterRepository.create(_)
	}

	def "create should require a password"() {
		when:
		recruiterService.create(1, validRecruiter(password: ""))

		then:
		thrown(ValidationException)
		0 * recruiterRepository.create(_)
	}

	def "update own profile should keep the current password when the new one is #label"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(password: null)

		when:
		Recruiter result = recruiterService.updateOwnProfile(1, "Nova descrição", password)

		then:
		1 * recruiterRepository.updateProfile(1, "Nova descrição", password) >> true
		result.description == "Nova descrição"
		result.password == null

		where:
		label   | password
		"empty" | ""
		"null"  | null
	}

	def "update own profile should save a valid new password and return the recruiter without it"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(id: 1, password: null)

		when:
		Recruiter result = recruiterService.updateOwnProfile(1, "Nova descrição", "novaSenha")

		then:
		1 * recruiterRepository.updateProfile(1, "Nova descrição", "novaSenha") >> true
		result.id == 1
		result.description == "Nova descrição"
		result.password == null
	}

	def "update own profile should reject a short password with the same message as the validator"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(password: null)
		String validatorMessage = validatorMessageFor(validRecruiter(password: "123"))

		when:
		recruiterService.updateOwnProfile(1, "Nova descrição", "123")

		then:
		ValidationException exception = thrown()
		exception.message == "A senha deve ter pelo menos 6 caracteres"
		exception.message == validatorMessage
		0 * recruiterRepository.updateProfile(*_)
	}

	def "update own profile should validate only the new password, not the loaded record"() {
		given:
		recruiterRepository.findById(1) >> validRecruiter(cpf: "000.000.000-00", password: null)

		when:
		recruiterService.updateOwnProfile(1, "Nova descrição", "")

		then:
		notThrown(ValidationException)
		1 * recruiterRepository.updateProfile(1, "Nova descrição", "") >> true
	}

	private static String validatorMessageFor(Recruiter recruiter) {
		try {
			new RecruiterValidator().validateForUpdate(recruiter)
			return null
		} catch (ValidationException exception) {
			return exception.message
		}
	}

	def "update own profile should fail with 404 when the recruiter does not exist"() {
		given:
		recruiterRepository.findById(99) >> null

		when:
		recruiterService.updateOwnProfile(99, "Nova descrição", "")

		then:
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID 99 não existe"
		0 * recruiterRepository.updateProfile(*_)
	}

	def "update own profile should fail with 404 when the recruiter is removed before the update"() {
		given:
		recruiterRepository.findById(5) >> validRecruiter(id: 5, password: null)

		when:
		recruiterService.updateOwnProfile(5, "Nova descrição", "")

		then:
		1 * recruiterRepository.updateProfile(5, "Nova descrição", "") >> false
		EntityNotFoundException exception = thrown()
		exception.message == "Recrutador com ID 5 não existe"
	}
}
