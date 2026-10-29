package com.mariajuliasales.dao

import com.mariajuliasales.model.Address
import com.mariajuliasales.util.DocumentNormalizer

import java.sql.Connection
import java.sql.ResultSet

final class AddressDAO {

	static final String COLUMNS = 'a.id AS address_id, a.cep, a.street, a.number, a.complement, a.neighborhood, ' +
	'a.city, a.state, a.country'

	private static final String INSERT_ADDRESS = '''
            INSERT INTO address (cep, street, number, complement, neighborhood, city, state, country)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id'''

	static int insert(Connection connection, Address address) {
		int addressId = SqlExecutor.insertReturningId(connection, INSERT_ADDRESS, [
			DocumentNormalizer.onlyDigits(address.cep) ?: null,
			address.street,
			address.number,
			address.complement,
			address.neighborhood,
			address.city,
			address.state?.toUpperCase(),
			address.country ?: Address.DEFAULT_COUNTRY
		])
		address.id = addressId
		addressId
	}

	static Address mapRow(ResultSet row) {
		new Address(
				id: row.getInt('address_id'),
				cep: row.getString('cep'),
				street: row.getString('street'),
				number: row.getString('number'),
				complement: row.getString('complement'),
				neighborhood: row.getString('neighborhood'),
				city: row.getString('city'),
				state: row.getString('state'),
				country: row.getString('country')
				)
	}
}
