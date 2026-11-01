package com.mariajuliasales.view

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.response.AddressResponse

class AddressForm {

	private static final String DEFAULT_COUNTRY = 'Brasil'

	private final ConsoleInput input
	private final ConsoleView view

	AddressForm(ConsoleInput input, ConsoleView view) {
		this.input = input
		this.view = view
	}

	AddressRequest read(String title, AddressResponse current) {
		view.showMessage(title)
		new AddressRequest(
				input.readText('CEP', current?.cep()),
				input.readText('Rua (opcional)', current?.street()),
				input.readText('Número (opcional)', current?.number()),
				input.readText('Complemento (opcional)', current?.complement()),
				input.readText('Bairro (opcional)', current?.neighborhood()),
				input.readText('Cidade', current?.city()),
				input.readText('Estado (ex: SP, MG)', current?.state()),
				input.readText('País', current?.country() ?: DEFAULT_COUNTRY)
				)
	}
}
