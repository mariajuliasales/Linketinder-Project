package com.mariajuliasales.dto.response

record AddressResponse(
        String cep,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String country
) {
    @Override
    String toString() {
        String streetLine = [[street, number].findAll().join(', '), complement, neighborhood].findAll().join(' - ')
        [streetLine, "${city}/${state} - ${country}", cep ? "CEP ${cep}" : null].findAll().join(', ')
    }
}
