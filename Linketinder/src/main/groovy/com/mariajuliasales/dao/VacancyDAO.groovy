package com.mariajuliasales.dao

import com.mariajuliasales.model.Enterprise
import com.mariajuliasales.model.Vacancy
import com.mariajuliasales.model.VacancyStatus

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class VacancyDAO {

    private static final String SELECT_VACANCY = """
            SELECT v.id, v.title, v.description, v.status, ${AddressDAO.COLUMNS},
                   e.id AS enterprise_id, e.cnpj, p.name AS enterprise_name, p.email AS enterprise_email
            FROM vacancy v
            JOIN address a    ON a.id = v.address_id
            JOIN enterprise e ON e.id = v.enterprise_id
            JOIN person p     ON p.id = e.person_id""".toString()

    private final DatabaseConnection database
    private final CompetenceDAO competenceDAO
    private final AddressDAO addressDAO = new AddressDAO()

    VacancyDAO(DatabaseConnection database, CompetenceDAO competenceDAO) {
        this.database = database
        this.competenceDAO = competenceDAO
    }

    Vacancy create(Vacancy vacancy) {
        database.withTransaction { Connection connection ->
            int addressId = addressDAO.insert(connection, vacancy.address)

            String sql = '''INSERT INTO vacancy (enterprise_id, title, description, status, address_id)
                            VALUES (?, ?, ?, ?::vacancy_status, ?)
                            RETURNING id'''
            connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
                statement.setInt(1, vacancy.enterprise.id)
                statement.setString(2, vacancy.title)
                statement.setString(3, vacancy.description)
                statement.setString(4, (vacancy.status ?: VacancyStatus.OPEN).name())
                statement.setInt(5, addressId)
                statement.executeQuery().withCloseable { ResultSet result ->
                    result.next()
                    vacancy.id = result.getInt('id')
                }
            }

            saveCompetences(connection, vacancy)
            vacancy
        }
    }

    List<Vacancy> findAll() {
        findWhere('', null)
    }

    Vacancy findById(int id) {
        findWhere('WHERE v.id = ?', id)[0]
    }

    List<Vacancy> findByEnterprise(int enterpriseId) {
        findWhere('WHERE v.enterprise_id = ?', enterpriseId)
    }

    List<Vacancy> findByEnterprise(Connection connection, int enterpriseId) {
        findWhere(connection, 'WHERE v.enterprise_id = ?', enterpriseId)
    }

    Vacancy update(Vacancy vacancy) {
        database.withTransaction { Connection connection ->
            int addressId = addressDAO.insert(connection, vacancy.address)

            String sql = '''UPDATE vacancy
                            SET title = ?, description = ?, status = ?::vacancy_status, address_id = ?
                            WHERE id = ?'''
            int updated = connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
                statement.setString(1, vacancy.title)
                statement.setString(2, vacancy.description)
                statement.setString(3, (vacancy.status ?: VacancyStatus.OPEN).name())
                statement.setInt(4, addressId)
                statement.setInt(5, vacancy.id)
                statement.executeUpdate()
            }
            if (updated == 0) {
                connection.rollback()
                return null
            }

            saveCompetences(connection, vacancy)
            vacancy
        }
    }

    boolean delete(int id) {
        database.withConnection { Connection connection ->
            connection.prepareStatement('DELETE FROM vacancy WHERE id = ?').withCloseable { PreparedStatement statement ->
                statement.setInt(1, id)
                statement.executeUpdate() > 0
            }
        }
    }


    private void saveCompetences(Connection connection, Vacancy vacancy) {
        competenceDAO.replaceLinks(connection, 'vacancy_competence', 'vacancy_id', vacancy.id, vacancy.competences)
    }

    private List<Vacancy> findWhere(String where, Integer parameter) {
        database.withConnection { Connection connection -> findWhere(connection, where, parameter) }
    }

    private List<Vacancy> findWhere(Connection connection, String where, Integer parameter) {
        connection.prepareStatement("${SELECT_VACANCY} ${where} ORDER BY v.id".toString()).withCloseable { PreparedStatement statement ->
            if (parameter != null) {
                statement.setInt(1, parameter)
            }
            statement.executeQuery().withCloseable { ResultSet result -> readAll(connection, result) }
        }
    }

    private List<Vacancy> readAll(Connection connection, ResultSet result) {
        List<Vacancy> vacancies = []
        Map<Integer, Enterprise> enterprises = [:]
        while (result.next()) {
            Vacancy vacancy = new Vacancy(
                    id: result.getInt('id'),
                    title: result.getString('title'),
                    description: result.getString('description'),
                    status: VacancyStatus.valueOf(result.getString('status')),
                    address: AddressDAO.read(result)
            )
            Enterprise enterprise = enterprises.computeIfAbsent(result.getInt('enterprise_id')) { Integer id ->
                new Enterprise(
                        id: id,
                        name: result.getString('enterprise_name'),
                        email: result.getString('enterprise_email'),
                        cnpj: result.getString('cnpj')
                )
            }
            enterprise.addVacancy(vacancy)
            vacancies << vacancy
        }
        vacancies.each { Vacancy vacancy ->
            vacancy.competences = competenceDAO.findLinked(connection, 'vacancy_competence', 'vacancy_id', vacancy.id)
        }
        vacancies
    }
}