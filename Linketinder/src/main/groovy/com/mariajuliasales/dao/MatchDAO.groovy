package com.mariajuliasales.dao

import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus
import com.mariajuliasales.repository.MatchRepository

import java.sql.Connection
import java.sql.ResultSet

/**
 * SQL dos matches. Cada método é uma única instrução e usa a Connection da transação do serviço.
 * O INSERT usa ON CONFLICT DO NOTHING. As consultas selecionam só ids, título e status, nunca colunas de person.
 * likedVacanciesOfEnterprise trava as vagas lidas (FOR SHARE OF v): até o fim da transação, elas não mudam de status
 * nem somem. Ordem das travas: sempre enterprise antes de vacancy.
 */
class MatchDAO implements MatchRepository {

	private static final String LIKED_VACANCIES_OF_ENTERPRISE = '''
            SELECT cl.vacancy_id AS id, v.status
            FROM candidate_like cl
            JOIN vacancy v ON v.id = cl.vacancy_id
            WHERE cl.candidate_id = ?
              AND v.enterprise_id = ?
            ORDER BY cl.vacancy_id
            FOR SHARE OF v'''
	private static final String INSERT_MATCH =
	'INSERT INTO vacancy_match (candidate_id, vacancy_id) VALUES (?, ?) ON CONFLICT DO NOTHING'
	private static final String DELETE_MATCHES_OF_CANDIDATE_WITH_ENTERPRISE = '''
            DELETE FROM vacancy_match vm
            USING vacancy v
            WHERE v.id = vm.vacancy_id
              AND vm.candidate_id = ?
              AND v.enterprise_id = ?'''
	private static final String MATCHED_VACANCIES_BY_ENTERPRISE = '''
            SELECT vm.candidate_id, v.id, v.title, v.status
            FROM vacancy_match vm
            JOIN vacancy v ON v.id = vm.vacancy_id
            WHERE v.enterprise_id = ?
            ORDER BY vm.candidate_id, v.id'''

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

	@Override
	Map<Integer, List<Vacancy>> matchedVacanciesByEnterprise(Connection connection, int enterpriseId) {
		Map<Integer, List<Vacancy>> vacanciesByCandidate = [:]
		SqlExecutor.query(connection, MATCHED_VACANCIES_BY_ENTERPRISE, [enterpriseId]) { ResultSet row ->
			Vacancy vacancy = new Vacancy(
					id: row.getInt('id'),
					title: row.getString('title'),
					status: VacancyStatus.valueOf(row.getString('status'))
					)
			vacanciesByCandidate.computeIfAbsent(row.getInt('candidate_id')) { [] } << vacancy
		}
		vacanciesByCandidate
	}
}
