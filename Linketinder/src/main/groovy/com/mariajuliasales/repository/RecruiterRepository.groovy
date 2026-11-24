package com.mariajuliasales.repository

import com.mariajuliasales.model.Recruiter

import java.sql.Connection

/**
 * Persistência do recrutador. Não estende Repository<T>: o recrutador não tem findAll global, update completo
 * nem delete com transação própria (a remoção roda dentro da transação do serviço).
 * Nenhuma consulta devolve a senha.
 */
interface RecruiterRepository {

	/** Grava person (sem endereço) e recruiter na mesma transação e devolve o recrutador com o id preenchido. */
	Recruiter create(Recruiter recruiter)

	Recruiter findById(int id)

	/** Devolve null se o recrutador não existe ou é de outra empresa. */
	Recruiter findByIdAndEnterprise(int id, int enterpriseId)

	List<Recruiter> findAllByEnterprise(int enterpriseId)

	/** E-mail em uso por qualquer pessoa (candidato, empresa ou recrutador), sem diferenciar maiúsculas. */
	boolean isEmailInUse(String email)

	/** Atualiza a descrição e, se password tiver texto, a senha. Devolve false se o recrutador não existe. */
	boolean updateProfile(int recruiterId, String description, String password)

	/** Apaga person e, em cascata, recruiter e recruiter_like, dentro da transação recebida. */
	boolean delete(Connection connection, int id)
}
