package com.mariajuliasales.dao

import com.mariajuliasales.model.Candidate
import com.mariajuliasales.util.ValidateUtil

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate

class CandidateDAO extends PersonDAO {

    private static final String SELECT_CANDIDATE = """
            SELECT c.id, c.person_id, c.cpf, c.birth_date, c.training, ${PERSON_COLUMNS}
            FROM candidate c
            JOIN person p  ON p.id = c.person_id
            JOIN address a ON a.id = p.address_id""".toString()

    CandidateDAO(DatabaseConnection database, CompetenceDAO competenceDAO) {
        super(database, competenceDAO)
    }

    Candidate create(Candidate candidate) {
        database.withTransaction { Connection connection ->
            int personId = insertPerson(connection, candidate, 'CANDIDATE')

            String sql = 'INSERT INTO candidate (person_id, cpf, birth_date, training) VALUES (?, ?, ?, ?) RETURNING id'
            connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
                statement.setInt(1, personId)
                statement.setString(2, ValidateUtil.onlyDigits(candidate.cpf))
                statement.setObject(3, candidate.birthDate)
                statement.setString(4, candidate.training)
                statement.executeQuery().withCloseable { ResultSet result ->
                    result.next()
                    candidate.id = result.getInt('id')
                }
            }

            saveCompetences(connection, candidate)
            candidate
        }
    }

    List<Candidate> findAll() {
        database.withConnection { Connection connection ->
            connection.prepareStatement("${SELECT_CANDIDATE} ORDER BY c.id".toString()).withCloseable { PreparedStatement statement ->
                statement.executeQuery().withCloseable { ResultSet result -> readAll(connection, result) }
            }
        }
    }

    Candidate findById(int id) {
        database.withConnection { Connection connection ->
            connection.prepareStatement("${SELECT_CANDIDATE} WHERE c.id = ?".toString()).withCloseable { PreparedStatement statement ->
                statement.setInt(1, id)
                statement.executeQuery().withCloseable { ResultSet result -> readAll(connection, result)[0] }
            }
        }
    }

    Candidate update(Candidate candidate) {
        database.withTransaction { Connection connection ->
            Integer personId = findPersonId(connection, candidate.id)
            if (personId == null) {
                return null
            }

            updatePerson(connection, personId, candidate)

            String sql = 'UPDATE candidate SET cpf = ?, birth_date = ?, training = ? WHERE id = ?'
            connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
                statement.setString(1, ValidateUtil.onlyDigits(candidate.cpf))
                statement.setObject(2, candidate.birthDate)
                statement.setString(3, candidate.training)
                statement.setInt(4, candidate.id)
                statement.executeUpdate()
            }

            saveCompetences(connection, candidate)
            candidate
        }
    }

    boolean delete(int id) {
        database.withTransaction { Connection connection ->
            Integer personId = findPersonId(connection, id)
            personId != null && deletePerson(connection, personId)
        }
    }



    private void saveCompetences(Connection connection, Candidate candidate) {
        competenceDAO.replaceLinks(connection, 'candidate_competence', 'candidate_id', candidate.id, candidate.competences)
    }

    private static Integer findPersonId(Connection connection, int candidateId) {
        connection.prepareStatement('SELECT person_id FROM candidate WHERE id = ?').withCloseable { PreparedStatement statement ->
            statement.setInt(1, candidateId)
            statement.executeQuery().withCloseable { ResultSet result ->
                result.next() ? result.getInt('person_id') : null
            }
        }
    }

    private List<Candidate> readAll(Connection connection, ResultSet result) {
        List<Candidate> candidates = []
        while (result.next()) {
            Candidate candidate = new Candidate(
                    id: result.getInt('id'),
                    cpf: result.getString('cpf'),
                    birthDate: result.getObject('birth_date', LocalDate),
                    training: result.getString('training')
            )
            fillPerson(candidate, result)
            candidates << candidate
        }

        candidates.each { Candidate candidate ->
            candidate.competences = competenceDAO.findLinked(connection, 'candidate_competence', 'candidate_id', candidate.id)
        }
        candidates
    }
}