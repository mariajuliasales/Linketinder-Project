package com.mariajuliasales.dao

import com.mariajuliasales.model.Enterprise

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class EnterpriseDAO extends PersonDAO {

    private static final String SELECT_ENTERPRISE = """
            SELECT e.id, e.person_id, e.cnpj, ${PERSON_COLUMNS}
            FROM enterprise e
            JOIN person p  ON p.id = e.person_id
            JOIN address a ON a.id = p.address_id""".toString()

    private final VacancyDAO vacancyDAO

    EnterpriseDAO(DatabaseConnection database, CompetenceDAO competenceDAO, VacancyDAO vacancyDAO) {
        super(database, competenceDAO)
        this.vacancyDAO = vacancyDAO
    }

    Enterprise create(Enterprise enterprise) {
        database.withTransaction { Connection connection ->
            int personId = insertPerson(connection, enterprise, 'ENTERPRISE')

            connection.prepareStatement('INSERT INTO enterprise (person_id, cnpj) VALUES (?, ?) RETURNING id').withCloseable { PreparedStatement statement ->
                statement.setInt(1, personId)
                statement.setString(2, normalizeCnpj(enterprise.cnpj))
                statement.executeQuery().withCloseable { ResultSet result ->
                    result.next()
                    enterprise.id = result.getInt('id')
                }
            }

            saveCompetences(connection, enterprise)
            enterprise
        }
    }

    List<Enterprise> findAll() {
        database.withConnection { Connection connection ->
            connection.prepareStatement("${SELECT_ENTERPRISE} ORDER BY e.id".toString()).withCloseable { PreparedStatement statement ->
                statement.executeQuery().withCloseable { ResultSet result -> readAll(connection, result) }
            }
        }
    }

    Enterprise findById(int id) {
        database.withConnection { Connection connection ->
            connection.prepareStatement("${SELECT_ENTERPRISE} WHERE e.id = ?".toString()).withCloseable { PreparedStatement statement ->
                statement.setInt(1, id)
                statement.executeQuery().withCloseable { ResultSet result -> readAll(connection, result)[0] }
            }
        }
    }

    Enterprise update(Enterprise enterprise) {
        database.withTransaction { Connection connection ->
            Integer personId = findPersonId(connection, enterprise.id)
            if (personId == null) {
                return null
            }

            updatePerson(connection, personId, enterprise)

            connection.prepareStatement('UPDATE enterprise SET cnpj = ? WHERE id = ?').withCloseable { PreparedStatement statement ->
                statement.setString(1, normalizeCnpj(enterprise.cnpj))
                statement.setInt(2, enterprise.id)
                statement.executeUpdate()
            }

            saveCompetences(connection, enterprise)
            enterprise
        }
    }

    boolean delete(int id) {
        database.withTransaction { Connection connection ->
            Integer personId = findPersonId(connection, id)
            personId != null && deletePerson(connection, personId)
        }
    }



    private void saveCompetences(Connection connection, Enterprise enterprise) {
        competenceDAO.replaceLinks(connection, 'enterprise_competence', 'enterprise_id', enterprise.id, enterprise.competences)
    }

    private static String normalizeCnpj(String cnpj) {
        cnpj?.replaceAll(/[.\-\/\s]/, '')?.toUpperCase()
    }

    private static Integer findPersonId(Connection connection, int enterpriseId) {
        connection.prepareStatement('SELECT person_id FROM enterprise WHERE id = ?').withCloseable { PreparedStatement statement ->
            statement.setInt(1, enterpriseId)
            statement.executeQuery().withCloseable { ResultSet result ->
                result.next() ? result.getInt('person_id') : null
            }
        }
    }

    private List<Enterprise> readAll(Connection connection, ResultSet result) {
        List<Enterprise> enterprises = []
        while (result.next()) {
            Enterprise enterprise = new Enterprise(id: result.getInt('id'), cnpj: result.getString('cnpj'))
            fillPerson(enterprise, result)
            enterprises << enterprise
        }
        enterprises.each { Enterprise enterprise ->
            enterprise.competences = competenceDAO.findLinked(connection, 'enterprise_competence', 'enterprise_id', enterprise.id)
            vacancyDAO.findByEnterprise(connection, enterprise.id).each { enterprise.addVacancy(it) }
        }
        enterprises
    }
}