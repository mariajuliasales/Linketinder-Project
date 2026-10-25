package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.response.AddressResponse
import com.mariajuliasales.model.Address

final class AddressMapper {

	static Address toEntity(AddressRequest request) {
		if (request == null) {
			return null
		}
		new Address(
				cep: request.cep(),
				street: blankToNull(request.street()),
				number: blankToNull(request.number()),
				complement: blankToNull(request.complement()),
				neighborhood: blankToNull(request.neighborhood()),
				city: request.city(),
				state: request.state(),
				country: request.country() ?: Address.DEFAULT_COUNTRY
				)
	}

	static AddressResponse toResponse(Address address) {
		if (address == null) {
			return null
		}
		new AddressResponse(
				address.cep,
				address.street,
				address.number,
				address.complement,
				address.neighborhood,
				address.city,
				address.state,
				address.country
				)
	}

	private static String blankToNull(String value) {
		value?.trim() ? value : null
	}
}
