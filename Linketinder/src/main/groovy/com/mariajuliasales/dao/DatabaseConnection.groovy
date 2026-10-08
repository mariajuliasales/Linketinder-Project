package com.mariajuliasales.dao

import org.postgresql.util.PSQLException

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnection {

    final String url
    final String user
    final String password

    DatabaseConnection(
            String url = System.getenv('DB_URL') ?: 'jdbc:postgresql://localhost:5432/linketinder',
            String user = System.getenv('DB_USER') ?: 'postgres',
            String password = System.getenv('DB_PASSWORD') ?: 'postgres'
    ) {
        this.url = url
        this.user = user
        this.password = password
    }

    Connection getConnection() {
        DriverManager.getConnection(url, user, password)
    }

    def <T> T withConnection(Closure<T> work) {
        try {
            getConnection().withCloseable { Connection connection -> work(connection) }
        } catch (SQLException e) {
            throw translate(e)
        }
    }

    def <T> T withTransaction(Closure<T> work) {
        withConnection { Connection connection ->
            connection.autoCommit = false
            try {
                T result = work(connection)
                connection.commit()
                result
            } catch (Exception e) {
                connection.rollback()
                throw e
            }
        }
    }

    private static Exception translate(SQLException e) {
        String detail = (e instanceof PSQLException) ? e.serverErrorMessage?.detail : null
        switch (e.SQLState) {
            case '23505':
                return new IllegalArgumentException("Record already exists. ${detail ?: e.message}", e)
            case '23503':   // foreign_key_violation
            case '23001':   // restrict_violation (ON DELETE RESTRICT)
                return new IllegalStateException("Record is used by other records. ${detail ?: e.message}", e)
            default:
                return e
        }
    }
}
