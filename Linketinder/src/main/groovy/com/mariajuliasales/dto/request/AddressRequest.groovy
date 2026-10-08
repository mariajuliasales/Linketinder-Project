package com.mariajuliasales.dto.request

record AddressRequest(
        String cep,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String country
) {
}
