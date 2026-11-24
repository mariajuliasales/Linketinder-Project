package com.mariajuliasales.api

import com.mariajuliasales.controller.RecruiterController
import com.mariajuliasales.dto.request.RecruiterRequest
import com.mariajuliasales.dto.response.RecruiterResponse
import com.mariajuliasales.exception.ValidationException
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

import java.time.LocalDate

/** Única rota do recrutador: a empresa cadastra um recrutador. A resposta nunca traz cpf, birthDate, age ou password. */
class RecruiterServlet extends HttpServlet {

	private final RecruiterController controller

	RecruiterServlet(RecruiterController controller) {
		this.controller = controller
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.characterEncoding = 'UTF-8'
			Map json = new JsonSlurper().parse(request.reader) as Map

			if (!(json.enterpriseId instanceof Integer)) {
				throw new ValidationException('O campo enterpriseId é obrigatório e deve ser um número')
			}
			RecruiterRequest dados = new RecruiterRequest(
					json.name,
					json.email,
					json.password,
					json.description,
					json.cpf,
					json.birthDate ? LocalDate.parse(json.birthDate) : null,
					json.area
					)
			RecruiterResponse criado = controller.create(json.enterpriseId as int, dados)

			response.setHeader('Location', "/api/recruiters/${criado.id()}")
			JsonResponse.send(response, 201, criado)
		} catch (Exception exception) {
			JsonResponse.sendError(response, exception)
		}
	}
}
