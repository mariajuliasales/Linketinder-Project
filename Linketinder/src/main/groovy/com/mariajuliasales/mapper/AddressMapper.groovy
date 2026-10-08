package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.response.AddressResponse
import com.mariajuliasales.model.Address

class AddressMapper {

    static Address toAddress(AddressRequest request) {

        if(!request)
            throw new IllegalArgumentException("AddressRequest cannot be null")

        new Address(
                cep: request.cep(),
                street: request.street() ?: null,
                number: request.number() ?: null,
                complement: request.complement() ?: null,
                neighborhood: request.neighborhood() ?: null,
                city: request.city(),
                state: request.state(),
                country: request.country() ?: 'Brasil'
        )
    }

    static AddressResponse toAddressResponse(Address address) {

        if(!address)
            throw new IllegalArgumentException("Address cannot be null")

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
}
