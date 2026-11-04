package com.mariajuliasales.api

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import groovy.json.JsonException
import groovy.json.JsonGenerator
import jakarta.servlet.http.HttpServletResponse

import java.time.LocalDate
import java.time.format.DateTimeParseException

class JsonResponse {

	private static final JsonGenerator JSON = new JsonGenerator.Options()
	.addConverter(LocalDate) { LocalDate date -> date.toString() }
	.disableUnicodeEscaping()
	.build()

	static void send(HttpServletResponse response, int status, Object body) {
		response.status = status
		response.contentType = 'application/json;charset=UTF-8'
		response.writer.write(JSON.toJson(body))
	}

	static void sendError(HttpServletResponse response, Exception exception) {
		switch (exception) {
			case ValidationException:
				send(response, 400, [error: exception.message])
				break
			case JsonException:
			case DateTimeParseException:
				send(response, 400, [error: 'JSON inválido'])
				break
			case EntityNotFoundException:
				send(response, 404, [error: exception.message])
				break
			case DuplicateEntityException:
				send(response, 409, [error: exception.message])
				break
			default:
				exception.printStackTrace()
				send(response, 500, [error: 'Erro interno do servidor'])
		}
	}
}
