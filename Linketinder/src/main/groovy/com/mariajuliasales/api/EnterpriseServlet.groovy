package com.mariajuliasales.api

import com.mariajuliasales.controller.EnterpriseController
import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.request.EnterpriseRequest
import com.mariajuliasales.dto.response.EnterpriseResponse
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class EnterpriseServlet extends HttpServlet {

	private final EnterpriseController controller

	EnterpriseServlet(EnterpriseController controller) {
		this.controller = controller
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.characterEncoding = 'UTF-8'
			Map json = new JsonSlurper().parse(request.reader) as Map

			EnterpriseRequest dados = new EnterpriseRequest(
					json.name,
					json.email,
					json.password,
					toAddress(json.address as Map),
					json.description,
					json.competences ?: [],
					json.cnpj
					)
			EnterpriseResponse criada = controller.create(dados)

			response.setHeader('Location', "/api/enterprises/${criada.id()}")
			JsonResponse.send(response, 201, criada)
		} catch (Exception exception) {
			JsonResponse.sendError(response, exception)
		}
	}

	private static AddressRequest toAddress(Map address) {
		if (!address) {
			return null
		}
		new AddressRequest(address.cep, address.street, address.number, address.complement,
				address.neighborhood, address.city, address.state, address.country)
	}
}
