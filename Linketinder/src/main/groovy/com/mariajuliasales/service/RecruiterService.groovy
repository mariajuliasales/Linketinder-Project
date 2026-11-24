package com.mariajuliasales.service

import com.mariajuliasales.dao.ConnectionManager
import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.model.Recruiter
import com.mariajuliasales.repository.EnterpriseRepository
import com.mariajuliasales.repository.LikeRepository
import com.mariajuliasales.repository.RecruiterRepository
import com.mariajuliasales.service.validator.Validator
import com.mariajuliasales.util.PasswordValidator

import java.sql.Connection

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
	private final ConnectionManager connectionManager
	private final LikeRepository likeRepository
	private final RecruiterLikeService recruiterLikeService

	RecruiterService(RecruiterRepository recruiterRepository, EnterpriseRepository enterpriseRepository,
			Validator<Recruiter> validator, ConnectionManager connectionManager, LikeRepository likeRepository,
			RecruiterLikeService recruiterLikeService) {
		this.recruiterRepository = recruiterRepository
		this.enterpriseRepository = enterpriseRepository
		this.validator = validator
		this.connectionManager = connectionManager
		this.likeRepository = likeRepository
		this.recruiterLikeService = recruiterLikeService
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

	/** Só os recrutadores da própria empresa. 404 "Empresa com ID <id> não existe" se a empresa não existir. */
	List<Recruiter> listByEnterprise(int enterpriseId) {
		requireFound(enterpriseRepository.findById(enterpriseId), ENTERPRISE, enterpriseId)
		recruiterRepository.findAllByEnterprise(enterpriseId)
	}

	/** Empresa do recrutador. 404 "Recrutador com ID <id> não existe" se o recrutador não existir. */
	int enterpriseIdOf(int recruiterId) {
		requireFound(recruiterRepository.findById(recruiterId), RECRUITER, recruiterId).enterpriseId
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

	/**
	 * Remoção do recrutador pela própria empresa. 404 "Recrutador com ID <id> não existe" quando ele não existe ou é
	 * de outra empresa: a resposta não distingue os dois casos. Numa transação: trava E (404 se E sumiu, porque o
	 * recrutador cai junto em cascata) → para cada candidato C curtido por ele, apaga a recruiter_like e aplica a regra
	 * de "saiu o último" (RecruiterLikeService#releaseRecruiterLike) → apaga recruiter e person. Qualquer erro desfaz tudo.
	 */
	void removeByEnterprise(int enterpriseId, int recruiterId) {
		requireFound(recruiterRepository.findByIdAndEnterprise(recruiterId, enterpriseId), RECRUITER, recruiterId)

		connectionManager.withTransaction { Connection connection ->
			if (!likeRepository.lockEnterprise(connection, enterpriseId)) {
				throw new EntityNotFoundException(RECRUITER, recruiterId)
			}
			likeRepository.candidatesLikedBy(connection, recruiterId).each { int candidateId ->
				recruiterLikeService.releaseRecruiterLike(connection, enterpriseId, recruiterId, candidateId)
			}
			if (!recruiterRepository.delete(connection, recruiterId)) {
				throw new EntityNotFoundException(RECRUITER, recruiterId)
			}
		}
	}
}
