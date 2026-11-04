package com.mariajuliasales.api

import com.mariajuliasales.controller.CandidateController
import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.dto.request.CandidateRequest
import com.mariajuliasales.dto.response.CandidateResponse
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import java.time.LocalDate

class CandidateServlet extends HttpServlet {

	private final CandidateController controller

	CandidateServlet(CandidateController controller) {
		this.controller = controller
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.characterEncoding = 'UTF-8'
			Map json = new JsonSlurper().parse(request.reader) as Map

			CandidateRequest dados = new CandidateRequest(
					json.name,
					json.email,
					json.password,
					toAddress(json.address as Map),
					json.description,
					json.competences ?: [],
					json.cpf,
					json.birthDate ? LocalDate.parse(json.birthDate) : null,
					json.training
					)
			CandidateResponse criado = controller.create(dados)

			response.setHeader('Location', "/api/candidates/${criado.id()}")
			JsonResponse.send(response, 201, criado)
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
