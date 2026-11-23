package com.mariajuliasales.service

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.service.validator.Validator
import com.mariajuliasales.util.PasswordValidator

import static com.mariajuliasales.exception.EntityNotFoundException.requireFound
import static com.mariajuliasales.service.validator.Validation.require

/**
 * Não estende o CrudService: o RecruiterRepository não implementa Repository<T> (sem findAll global nem update
 * completo). O CPF não é comparado com o de candidatos: o recrutador pode ter o mesmo CPF de um candidato.
 */
class RecruiterService {

	private static final String RECRUITER = 'Recrutador'
	private static final String ENTERPRISE = 'Empresa'

	private final RecruiterRepository recruiterRepository
	private final EnterpriseRepository enterpriseRepository
	private final Validator<Recruiter> validator

	RecruiterService(RecruiterRepository recruiterRepository, EnterpriseRepository enterpriseRepository,
			Validator<Recruiter> validator) {
		this.recruiterRepository = recruiterRepository
		this.enterpriseRepository = enterpriseRepository
		this.validator = validator
	}

	/** Valida, confere a empresa (404) e o e-mail entre todas as pessoas (409), e só então grava. */
	Recruiter create(int enterpriseId, Recruiter recruiter) {
		validator.validateForCreate(recruiter)
		requireFound(enterpriseRepository.findById(enterpriseId), ENTERPRISE, enterpriseId)
		if (recruiterRepository.isEmailInUse(recruiter.email)) {
			throw new DuplicateEntityException('E-mail já cadastrado')
		}
		recruiter.enterpriseId = enterpriseId
		recruiterRepository.create(recruiter)
	}

	/**
	 * Atualiza a descrição e, se vier preenchida, a senha. Senha vazia mantém a atual. Só a senha nova é validada,
	 * com a mesma mensagem do RecruiterValidator. Devolve o recrutador carregado, com a nova descrição e sem senha.
	 */
	Recruiter updateOwnProfile(int recruiterId, String description, String password) {
		Recruiter recruiter = requireFound(recruiterRepository.findById(recruiterId), RECRUITER, recruiterId)
		if (password) {
			require(PasswordValidator.isValid(password),
					"A senha deve ter pelo menos ${PasswordValidator.MIN_LENGTH} caracteres".toString())
		}
		if (!recruiterRepository.updateProfile(recruiterId, description, password)) {
			throw new EntityNotFoundException(RECRUITER, recruiterId)
		}
		recruiter.description = description
		recruiter
	}
}
