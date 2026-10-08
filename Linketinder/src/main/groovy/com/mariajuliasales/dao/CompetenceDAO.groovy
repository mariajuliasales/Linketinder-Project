package com.mariajuliasales.dao

import com.mariajuliasales.model.Competence

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class CompetenceDAO {

    private final DatabaseConnection database

    CompetenceDAO(DatabaseConnection database) {
        this.database = database
    }

    Competence create(Competence competence) {
        database.withConnection { Connection connection ->
            insert(connection, competence.name)
        }
    }

    List<Competence> findAll() {
        database.withConnection { Connection connection ->
            connection.prepareStatement('SELECT id, name FROM competence ORDER BY name').withCloseable { PreparedStatement statement ->
                statement.executeQuery().withCloseable { ResultSet result -> readAll(result) }
            }
        }
    }

    Competence findById(int id) {
        database.withConnection { Connection connection ->
            connection.prepareStatement('SELECT id, name FROM competence WHERE id = ?').withCloseable { PreparedStatement statement ->
                statement.setInt(1, id)
                statement.executeQuery().withCloseable { ResultSet result -> readAll(result)[0] }
            }
        }
    }

    Competence findByName(String name) {
        database.withConnection { Connection connection -> selectByName(connection, name) }
    }

    Competence update(Competence competence) {
        database.withConnection { Connection connection ->
            connection.prepareStatement('UPDATE competence SET name = ? WHERE id = ?').withCloseable { PreparedStatement statement ->
                statement.setString(1, competence.name)
                statement.setInt(2, competence.id)
                statement.executeUpdate() > 0 ? competence : null
            }
        }
    }

    boolean delete(int id) {
        database.withConnection { Connection connection ->
            connection.prepareStatement('DELETE FROM competence WHERE id = ?').withCloseable { PreparedStatement statement ->
                statement.setInt(1, id)
                statement.executeUpdate() > 0
            }
        }
    }



    Competence findOrCreate(Connection connection, String name) {
        selectByName(connection, name) ?: insert(connection, name)
    }

    void replaceLinks(Connection connection, String table, String ownerColumn, int ownerId, List<Competence> competences) {
        connection.prepareStatement("DELETE FROM ${table} WHERE ${ownerColumn} = ?".toString()).withCloseable { PreparedStatement statement ->
            statement.setInt(1, ownerId)
            statement.executeUpdate()
        }

        String insertLink = "INSERT INTO ${table} (${ownerColumn}, competence_id) VALUES (?, ?) ON CONFLICT DO NOTHING".toString()
        connection.prepareStatement(insertLink).withCloseable { PreparedStatement statement ->
            competences.findAll { it?.name }.unique().each { Competence competence ->
                Competence saved = findOrCreate(connection, competence.name)
                competence.id = saved.id
                statement.setInt(1, ownerId)
                statement.setInt(2, saved.id)
                statement.executeUpdate()
            }
        }
    }

    List<Competence> findLinked(Connection connection, String table, String ownerColumn, int ownerId) {
        String sql = """SELECT co.id, co.name
                        FROM ${table} link
                        JOIN competence co ON co.id = link.competence_id
                        WHERE link.${ownerColumn} = ?
                        ORDER BY co.name""".toString()
        connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
            statement.setInt(1, ownerId)
            statement.executeQuery().withCloseable { ResultSet result -> readAll(result) }
        }
    }


    private static Competence insert(Connection connection, String name) {
        connection.prepareStatement('INSERT INTO competence (name) VALUES (?) RETURNING id, name').withCloseable { PreparedStatement statement ->
            statement.setString(1, name.trim())
            statement.executeQuery().withCloseable { ResultSet result -> readAll(result)[0] }
        }
    }

    private static Competence selectByName(Connection connection, String name) {
        connection.prepareStatement('SELECT id, name FROM competence WHERE lower(name) = lower(?)').withCloseable { PreparedStatement statement ->
            statement.setString(1, name?.trim())
            statement.executeQuery().withCloseable { ResultSet result -> readAll(result)[0] }
        }
    }

    private static List<Competence> readAll(ResultSet result) {
        List<Competence> competences = []
        while (result.next()) {
            competences << new Competence(result.getInt('id'), result.getString('name'))
        }
        competences
    }
}