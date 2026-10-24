package com.mariajuliasales.menu

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.model.Address

class AddressForm {

	private final ConsoleInput input

	AddressForm(ConsoleInput input) {
		this.input = input
	}

	AddressRequest read(String title, Address current) {
		println title
		new AddressRequest(
				input.readText('CEP', current?.cep),
				input.readText('Rua (opcional)', current?.street),
				input.readText('Número (opcional)', current?.number),
				input.readText('Complemento (opcional)', current?.complement),
				input.readText('Bairro (opcional)', current?.neighborhood),
				input.readText('Cidade', current?.city),
				input.readText('Estado (ex: SP, MG)', current?.state),
				input.readText('País', current?.country ?: Address.DEFAULT_COUNTRY)
				)
	}
}
