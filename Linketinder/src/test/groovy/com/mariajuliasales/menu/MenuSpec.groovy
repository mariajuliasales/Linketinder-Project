package com.mariajuliasales.menu

import com.mariajuliasales.exception.ValidationException
import spock.lang.Specification

class MenuSpec extends Specification {

	ByteArrayOutputStream output = new ByteArrayOutputStream()

	static class TestMenu extends Menu {

		List<String> executed = []
		boolean exited = false

		TestMenu(ConsoleInput input, ConsoleView view) {
			super('Teste', input, view)
		}

		@Override
		protected List<MenuOption> getOptions() {
			[
				new MenuOption('Primeira', { executed << 'first' }),
				new MenuOption('Falha', { throw new ValidationException('Dado inválido') })
			]
		}

		@Override
		protected void onExit() {
			exited = true
		}
	}

	private TestMenu menuWith(String... lines) {
		PrintStream out = new PrintStream(output, true, 'UTF-8')
		new TestMenu(new ConsoleInput(new Scanner(lines.join('\n') + '\n'), out), new ConsoleView(out))
	}

	private String printed() {
		output.toString('UTF-8')
	}

	def "menu should show its title and options and run the chosen ones until the exit option"() {
		given:
		TestMenu menu = menuWith('1', '1', '0')

		when:
		menu.run()

		then:
		menu.executed == ['first', 'first']
		menu.exited
		printed().contains('--- Teste ---')
		printed().contains('1. Primeira')
		printed().contains('0. Voltar')
	}

	def "invalid options should be reported and ignored"() {
		given:
		TestMenu menu = menuWith('9', '-1', '1', '0')

		when:
		menu.run()

		then:
		menu.executed == ['first']
		printed().count('Opção inválida') == 2
	}

	def "business errors should be shown without stopping the menu"() {
		given:
		TestMenu menu = menuWith('2', '1', '0')

		when:
		menu.run()

		then:
		notThrown(ValidationException)
		printed().contains('Erro: Dado inválido')
		menu.executed == ['first']
		menu.exited
	}

	def "end of input should stop the menu"() {
		given:
		TestMenu menu = menuWith('1')

		when:
		menu.run()

		then:
		thrown(EndOfInputException)
		menu.executed == ['first']
		!menu.exited
	}
}
