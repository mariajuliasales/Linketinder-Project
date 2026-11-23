package com.mariajuliasales.dao

import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.MatchRepository

import java.sql.Connection
import java.sql.ResultSet

/**
 * SQL dos matches. Cada método é uma única instrução e usa a Connection da transação do serviço.
 * O INSERT usa ON CONFLICT DO NOTHING. As consultas selecionam só ids e status.
 */
class MatchDAO implements MatchRepository {

	private static final String LIKED_VACANCIES_OF_ENTERPRISE = '''
            SELECT cl.vacancy_id AS id, v.status
            FROM candidate_like cl
            JOIN vacancy v ON v.id = cl.vacancy_id
            WHERE cl.candidate_id = ?
              AND v.enterprise_id = ?
            ORDER BY cl.vacancy_id'''
	private static final String INSERT_MATCH =
	'INSERT INTO vacancy_match (candidate_id, vacancy_id) VALUES (?, ?) ON CONFLICT DO NOTHING'
	private static final String DELETE_MATCHES_OF_CANDIDATE_WITH_ENTERPRISE = '''
            DELETE FROM vacancy_match vm
            USING vacancy v
            WHERE v.id = vm.vacancy_id
              AND vm.candidate_id = ?
              AND v.enterprise_id = ?'''

	@Override
	Map<Integer, VacancyStatus> likedVacanciesOfEnterprise(Connection connection, int candidateId, int enterpriseId) {
		Map<Integer, VacancyStatus> statusByVacancy = [:]
		SqlExecutor.query(connection, LIKED_VACANCIES_OF_ENTERPRISE, [candidateId, enterpriseId]) { ResultSet row ->
			statusByVacancy[row.getInt('id')] = VacancyStatus.valueOf(row.getString('status'))
		}
		statusByVacancy
	}

	@Override
	boolean insertMatch(Connection connection, int candidateId, int vacancyId) {
		SqlExecutor.update(connection, INSERT_MATCH, [candidateId, vacancyId]) > 0
	}

	@Override
	int deleteMatchesOfCandidateWithEnterprise(Connection connection, int candidateId, int enterpriseId) {
		SqlExecutor.update(connection, DELETE_MATCHES_OF_CANDIDATE_WITH_ENTERPRISE, [candidateId, enterpriseId])
	}
}
