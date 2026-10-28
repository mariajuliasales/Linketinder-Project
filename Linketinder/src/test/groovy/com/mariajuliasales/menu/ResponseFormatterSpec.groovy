package com.mariajuliasales.menu

import com.mariajuliasales.dto.response.AddressResponse
import spock.lang.Specification
import spock.lang.Unroll

class ResponseFormatterSpec extends Specification {

	@Unroll
	def "address should be formatted as '#expected'"() {
		expect:
		ResponseFormatter.formatAddress(address) == expected

		where:
		address                                                                                      || expected
		new AddressResponse('01310000', 'Av. Paulista', '1000', 'Sala 1', 'Bela Vista', 'São Paulo', 'SP', 'Brasil') || 'Av. Paulista, 1000 - Sala 1 - Bela Vista, São Paulo/SP - Brasil, CEP 01310000'
		new AddressResponse('01310000', null, null, null, null, 'São Paulo', 'SP', 'Brasil')        || 'São Paulo/SP - Brasil, CEP 01310000'
		new AddressResponse(null, null, null, null, null, 'Recife', 'PE', 'Brasil')                 || 'Recife/PE - Brasil'
		null                                                                                         || ''
	}
}
