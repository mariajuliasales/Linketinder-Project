package com.mariajuliasales.menu

import com.mariajuliasales.model.VacancyStatus

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ConsoleInput {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern('dd/MM/yyyy')

	private final Scanner scanner
	private final PrintStream out

	ConsoleInput(Scanner scanner = new Scanner(System.in), PrintStream out = System.out) {
		this.scanner = scanner
		this.out = out
	}

	String readText(String prompt) {
		if (prompt) {
			out.println prompt
		}
		if (!scanner.hasNextLine()) {
			throw new EndOfInputException()
		}
		scanner.nextLine().trim()
	}

	String readText(String label, String current) {
		String value = readText(current ? "${label} [${current}]:" : "${label}:")
		value ?: current
	}

	int readInt(String prompt) {
		String value = readText(prompt)
		while (!value.isInteger()) {
			out.println 'Digite um número inteiro.'
			value = readText(prompt)
		}
		value.toInteger()
	}

	LocalDate readDate(String label, LocalDate current) {
		String prompt = current ? "${label} [${current.format(DATE_FORMAT)}]:" : label
		while (true) {
			String value = readText(prompt)
			if (!value && current) {
				return current
			}
			try {
				return LocalDate.parse(value, DATE_FORMAT)
			} catch (DateTimeParseException ignored) {
				out.println 'Data inválida. Use o formato dd/mm/aaaa.'
			}
		}
	}

	VacancyStatus readStatus(VacancyStatus current) {
		String prompt = "Status (${VacancyStatus.values().join(', ')}) [${current}]:"
		while (true) {
			String value = readText(prompt)
			if (!value) {
				return current
			}
			VacancyStatus status = VacancyStatus.values().find { it.name().equalsIgnoreCase(value) }
			if (status) {
				return status
			}
			out.println 'Status inválido.'
		}
	}
}
