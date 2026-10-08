package com.mariajuliasales.dao

import com.mariajuliasales.model.PersonAbstract

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

abstract class PersonDAO {

    protected static final String PERSON_COLUMNS = "p.name, p.email, p.description, ${AddressDAO.COLUMNS}".toString()

    protected final DatabaseConnection database
    protected final CompetenceDAO competenceDAO
    protected final AddressDAO addressDAO = new AddressDAO()

    PersonDAO(DatabaseConnection database, CompetenceDAO competenceDAO) {
        this.database = database
        this.competenceDAO = competenceDAO
    }

    protected int insertPerson(Connection connection, PersonAbstract person, String type) {
        int addressId = addressDAO.insert(connection, person.address)
        String sql = '''INSERT INTO person (type, name, email, description, password_hash, address_id)
                        VALUES (?::person_type, ?, ?, ?, crypt(?, gen_salt('bf')), ?)
                        RETURNING id'''
        connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
            statement.setString(1, type)
            statement.setString(2, person.name)
            statement.setString(3, person.email)
            statement.setString(4, person.description)
            statement.setString(5, person.password)
            statement.setInt(6, addressId)
            statement.executeQuery().withCloseable { ResultSet result ->
                result.next()
                result.getInt('id')
            }
        }
    }

    protected void updatePerson(Connection connection, int personId, PersonAbstract person) {
        int addressId = addressDAO.insert(connection, person.address)
        String sql = 'UPDATE person SET name = ?, email = ?, description = ?, address_id = ? WHERE id = ?'
        connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
            statement.setString(1, person.name)
            statement.setString(2, person.email)
            statement.setString(3, person.description)
            statement.setInt(4, addressId)
            statement.setInt(5, personId)
            statement.executeUpdate()
        }

        if (person.password) {
            connection.prepareStatement("UPDATE person SET password_hash = crypt(?, gen_salt('bf')) WHERE id = ?").withCloseable { PreparedStatement statement ->
                statement.setString(1, person.password)
                statement.setInt(2, personId)
                statement.executeUpdate()
            }
        }
    }

    protected boolean deletePerson(Connection connection, int personId) {
        connection.prepareStatement('DELETE FROM person WHERE id = ?').withCloseable { PreparedStatement statement ->
            statement.setInt(1, personId)
            statement.executeUpdate() > 0
        }
    }

    protected static void fillPerson(PersonAbstract person, ResultSet result) {
        person.name = result.getString('name')
        person.email = result.getString('email')
        person.description = result.getString('description')
        person.address = AddressDAO.read(result)
    }
}