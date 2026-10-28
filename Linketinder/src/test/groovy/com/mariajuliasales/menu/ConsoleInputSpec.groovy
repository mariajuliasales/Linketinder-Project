package com.mariajuliasales.menu

import com.mariajuliasales.model.VacancyStatus
import spock.lang.Specification

import java.time.LocalDate

class ConsoleInputSpec extends Specification {

	private static ConsoleInput inputWith(String... lines) {
		new ConsoleInput(new Scanner(lines.join('\n') + '\n'))
	}

	def "read text should trim the typed value"() {
		expect:
		inputWith('  Maria  ').readText('Nome:') == 'Maria'
	}

	def "read text with current value should keep it when Enter is pressed"() {
		expect:
		inputWith('').readText('Nome', 'Maria') == 'Maria'
		inputWith('Julia').readText('Nome', 'Maria') == 'Julia'
	}

	def "read int should ask again until an integer is typed"() {
		expect:
		inputWith('abc', '', '42').readInt('ID:') == 42
	}

	def "read date should ask again until a valid date is typed"() {
		expect:
		inputWith('2000-05-10', '10/05/2000').readDate('Nascimento:', null) == LocalDate.of(2000, 5, 10)
	}

	def "read date should keep the current date when Enter is pressed"() {
		given:
		LocalDate current = LocalDate.of(1995, 1, 20)

		expect:
		inputWith('').readDate('Nascimento', current) == current
	}

	def "read status should accept any case, keep the current one on Enter and ask again when invalid"() {
		expect:
		inputWith('closed').readStatus(VacancyStatus.OPEN) == VacancyStatus.CLOSED
		inputWith('').readStatus(VacancyStatus.PAUSED) == VacancyStatus.PAUSED
		inputWith('ARCHIVED', 'paused').readStatus(VacancyStatus.OPEN) == VacancyStatus.PAUSED
	}

	def "reading after the end of the input should throw EndOfInputException"() {
		given:
		ConsoleInput input = new ConsoleInput(new Scanner(''))

		when:
		input.readText('Nome:')

		then:
		thrown(EndOfInputException)
	}
}
