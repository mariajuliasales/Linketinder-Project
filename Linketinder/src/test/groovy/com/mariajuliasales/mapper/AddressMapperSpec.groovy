package com.mariajuliasales.mapper

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.response.AddressResponse
import com.mariajuliasales.model.Address
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validAddress

class AddressMapperSpec extends Specification {

	def "request should be converted to an address"() {
		given:
		AddressRequest request = new AddressRequest("01310-000", "Avenida Paulista", "1000", "Sala 1", "Bela Vista",
				"São Paulo", "SP", "Brasil")

		when:
		Address address = AddressMapper.toEntity(request)

		then:
		address.cep == "01310-000"
		address.street == "Avenida Paulista"
		address.number == "1000"
		address.complement == "Sala 1"
		address.neighborhood == "Bela Vista"
		address.city == "São Paulo"
		address.state == "SP"
		address.country == "Brasil"
	}

	def "blank optional fields should become null and the country should default to Brasil"() {
		given:
		AddressRequest request = new AddressRequest("01310-000", "", " ", null, "", "São Paulo", "SP", "")

		when:
		Address address = AddressMapper.toEntity(request)

		then:
		address.street == null
		address.number == null
		address.complement == null
		address.neighborhood == null
		address.country == Address.DEFAULT_COUNTRY
	}

	def "address should be converted to a response"() {
		when:
		AddressResponse response = AddressMapper.toResponse(validAddress())

		then:
		response == new AddressResponse("01310-000", "Avenida Paulista", "1000", null, null, "São Paulo", "SP", "Brasil")
	}

	def "null values should be mapped to null"() {
		expect:
		AddressMapper.toEntity(null) == null
		AddressMapper.toResponse(null) == null
	}
}
