package com.mariajuliasales.api

import com.mariajuliasales.controller.VacancyController
import com.mariajuliasales.dto.request.VacancyRequest
import com.mariajuliasales.dto.response.VacancyAnonymousResponse
import com.mariajuliasales.exception.ValidationException
import groovy.json.JsonSlurper
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class VacancyServlet extends HttpServlet {

	private final VacancyController controller

	VacancyServlet(VacancyController controller) {
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
			VacancyRequest dados = new VacancyRequest(
					json.title,
					json.description,
					json.city,
					json.state,
					json.status,
					json.competences ?: []
					)
			VacancyAnonymousResponse criada = controller.create(json.enterpriseId as int, dados)

			response.setHeader('Location', "/api/vacancies/${criada.id()}")
			JsonResponse.send(response, 201, criada)
		} catch (Exception exception) {
			JsonResponse.sendError(response, exception)
		}
	}
}
