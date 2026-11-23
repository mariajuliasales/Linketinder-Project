package com.mariajuliasales.dao

import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.LikeRepository

import java.sql.Connection
import java.sql.ResultSet

/**
 * SQL das curtidas. Cada método é uma única instrução e usa a Connection da transação do serviço.
 * Os INSERT usam ON CONFLICT DO NOTHING. As consultas selecionam só ids, status e EXISTS.
 * Ordem das travas: sempre enterprise antes de vacancy.
 */
class LikeDAO implements LikeRepository {

	private static final String LOCK_ENTERPRISE = 'SELECT id FROM enterprise WHERE id = ? FOR UPDATE'
	private static final String LOCK_VACANCY_STATUS = 'SELECT status FROM vacancy WHERE id = ? FOR SHARE'

	private static final String CANDIDATE_LIKE_EXISTS =
	'SELECT EXISTS (SELECT 1 FROM candidate_like WHERE candidate_id = ? AND vacancy_id = ?) AS found'
	private static final String INSERT_CANDIDATE_LIKE =
	'INSERT INTO candidate_like (candidate_id, vacancy_id) VALUES (?, ?) ON CONFLICT DO NOTHING'
	private static final String DELETE_CANDIDATE_LIKE = 'DELETE FROM candidate_like WHERE candidate_id = ? AND vacancy_id = ?'

	private static final String RECRUITER_LIKE_EXISTS =
	'SELECT EXISTS (SELECT 1 FROM recruiter_like WHERE recruiter_id = ? AND candidate_id = ?) AS found'
	private static final String INSERT_RECRUITER_LIKE =
	'INSERT INTO recruiter_like (recruiter_id, candidate_id) VALUES (?, ?) ON CONFLICT DO NOTHING'
	private static final String DELETE_RECRUITER_LIKE = 'DELETE FROM recruiter_like WHERE recruiter_id = ? AND candidate_id = ?'
	private static final String OTHER_RECRUITER_OF_ENTERPRISE_LIKES = '''
            SELECT EXISTS (
                SELECT 1
                FROM recruiter_like rl
                JOIN recruiter other ON other.id = rl.recruiter_id
                JOIN recruiter self  ON self.enterprise_id = other.enterprise_id
                WHERE self.id = ?
                  AND other.id <> self.id
                  AND rl.candidate_id = ?
            ) AS found'''
	private static final String CANDIDATES_LIKED_BY =
	'SELECT candidate_id AS id FROM recruiter_like WHERE recruiter_id = ? ORDER BY candidate_id'

	private static final String ENTERPRISE_LIKE_EXISTS =
	'SELECT EXISTS (SELECT 1 FROM enterprise_like WHERE enterprise_id = ? AND candidate_id = ?) AS found'
	private static final String INSERT_ENTERPRISE_LIKE =
	'INSERT INTO enterprise_like (enterprise_id, candidate_id) VALUES (?, ?) ON CONFLICT DO NOTHING'
	private static final String DELETE_ENTERPRISE_LIKE = 'DELETE FROM enterprise_like WHERE enterprise_id = ? AND candidate_id = ?'

	@Override
	boolean lockEnterprise(Connection connection, int enterpriseId) {
		SqlExecutor.queryFirst(connection, LOCK_ENTERPRISE, [enterpriseId]) { ResultSet row -> row.getInt('id') } != null
	}

	@Override
	VacancyStatus lockVacancyStatus(Connection connection, int vacancyId) {
		SqlExecutor.queryFirst(connection, LOCK_VACANCY_STATUS, [vacancyId]) { ResultSet row ->
			VacancyStatus.valueOf(row.getString('status'))
		}
	}

	@Override
	boolean candidateLikeExists(Connection connection, int candidateId, int vacancyId) {
		exists(connection, CANDIDATE_LIKE_EXISTS, [candidateId, vacancyId])
	}

	@Override
	boolean insertCandidateLike(Connection connection, int candidateId, int vacancyId) {
		SqlExecutor.update(connection, INSERT_CANDIDATE_LIKE, [candidateId, vacancyId]) > 0
	}

	@Override
	boolean deleteCandidateLike(Connection connection, int candidateId, int vacancyId) {
		SqlExecutor.update(connection, DELETE_CANDIDATE_LIKE, [candidateId, vacancyId]) > 0
	}

	@Override
	boolean recruiterLikeExists(Connection connection, int recruiterId, int candidateId) {
		exists(connection, RECRUITER_LIKE_EXISTS, [recruiterId, candidateId])
	}

	@Override
	boolean insertRecruiterLike(Connection connection, int recruiterId, int candidateId) {
		SqlExecutor.update(connection, INSERT_RECRUITER_LIKE, [recruiterId, candidateId]) > 0
	}

	@Override
	boolean deleteRecruiterLike(Connection connection, int recruiterId, int candidateId) {
		SqlExecutor.update(connection, DELETE_RECRUITER_LIKE, [recruiterId, candidateId]) > 0
	}

	@Override
	boolean otherRecruiterOfEnterpriseLikes(Connection connection, int recruiterId, int candidateId) {
		exists(connection, OTHER_RECRUITER_OF_ENTERPRISE_LIKES, [recruiterId, candidateId])
	}

	@Override
	List<Integer> candidatesLikedBy(Connection connection, int recruiterId) {
		SqlExecutor.query(connection, CANDIDATES_LIKED_BY, [recruiterId]) { ResultSet row -> row.getInt('id') }
	}

	@Override
	boolean enterpriseLikeExists(Connection connection, int enterpriseId, int candidateId) {
		exists(connection, ENTERPRISE_LIKE_EXISTS, [enterpriseId, candidateId])
	}

	@Override
	boolean insertEnterpriseLike(Connection connection, int enterpriseId, int candidateId) {
		SqlExecutor.update(connection, INSERT_ENTERPRISE_LIKE, [enterpriseId, candidateId]) > 0
	}

	@Override
	boolean deleteEnterpriseLike(Connection connection, int enterpriseId, int candidateId) {
		SqlExecutor.update(connection, DELETE_ENTERPRISE_LIKE, [enterpriseId, candidateId]) > 0
	}

	private static boolean exists(Connection connection, String sql, List<?> parameters) {
		SqlExecutor.queryFirst(connection, sql, parameters) { ResultSet row -> row.getBoolean('found') }
	}
}
