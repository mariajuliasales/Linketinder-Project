package com.mariajuliasales.api

import com.mariajuliasales.exception.DuplicateEntityException
import com.mariajuliasales.exception.EntityNotFoundException
import com.mariajuliasales.exception.ValidationException
import groovy.json.JsonException
import jakarta.servlet.http.HttpServletResponse
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeParseException

class JsonResponseSpec extends Specification {

	StringWriter body = new StringWriter()
	HttpServletResponse response = Mock()

	def setup() {
		response.getWriter() >> new PrintWriter(body)
	}

	def "send should write the status, the JSON content type and the body"() {
		when:
		JsonResponse.send(response, 201, [city: 'São Paulo', birthDate: LocalDate.of(1995, 5, 20)])

		then:
		1 * response.setStatus(201)
		1 * response.setContentType('application/json;charset=UTF-8')
		body.toString() == '{"city":"São Paulo","birthDate":"1995-05-20"}'
	}

	def "sendError should turn each exception into its HTTP status"() {
		when:
		JsonResponse.sendError(response, exception)

		then:
		1 * response.setStatus(status)
		body.toString() == "{\"error\":\"${message}\"}"

		where:
		exception                                                  || status | message
		new ValidationException('CPF inválido')                    || 400    | 'CPF inválido'
		new JsonException('unexpected end')                        || 400    | 'JSON inválido'
		new DateTimeParseException('bad date', '20/05/1995', 0)    || 400    | 'JSON inválido'
		new EntityNotFoundException('Empresa', 9)               || 404    | 'Empresa com ID 9 não existe'
		new DuplicateEntityException('E-mail já cadastrado')       || 409    | 'E-mail já cadastrado'
		new IllegalStateException('falha inesperada')              || 500    | 'Erro interno do servidor'
	}
}
