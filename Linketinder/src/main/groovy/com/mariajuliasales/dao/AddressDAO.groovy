package com.mariajuliasales.dao

import com.mariajuliasales.model.Address
import com.mariajuliasales.util.ValidateUtil

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class AddressDAO {

    static final String COLUMNS = 'a.id AS address_id, a.cep, a.street, a.number, a.complement, a.neighborhood, ' +
            'a.city, a.state, a.country'

    int insert(Connection connection, Address address) {
        String sql = '''INSERT INTO address (cep, street, number, complement, neighborhood, city, state, country)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        RETURNING id'''
        connection.prepareStatement(sql).withCloseable { PreparedStatement statement ->
            statement.setString(1, address.cep ? ValidateUtil.onlyDigits(address.cep) : null)
            statement.setString(2, address.street ?: null)
            statement.setString(3, address.number ?: null)
            statement.setString(4, address.complement ?: null)
            statement.setString(5, address.neighborhood ?: null)
            statement.setString(6, address.city)
            statement.setString(7, address.state?.toUpperCase())
            statement.setString(8, address.country ?: 'Brasil')
            statement.executeQuery().withCloseable { ResultSet result ->
                result.next()
                address.id = result.getInt('id')
            }
        }
    }

    static Address read(ResultSet result) {
        new Address(
                id: result.getInt('address_id'),
                cep: result.getString('cep'),
                street: result.getString('street'),
                number: result.getString('number'),
                complement: result.getString('complement'),
                neighborhood: result.getString('neighborhood'),
                city: result.getString('city'),
                state: result.getString('state'),
                country: result.getString('country')
        )
    }
}