package com.mariajuliasales.menu

import spock.lang.Specification

class MainMenuSpec extends Specification {

	ByteArrayOutputStream output = new ByteArrayOutputStream()
	PrintStream out = new PrintStream(output, true, 'UTF-8')

	static class FakeSubmenu extends Menu {

		int runs = 0

		FakeSubmenu(String title, ConsoleInput input, ConsoleView view) {
			super(title, input, view)
		}

		@Override
		protected List<MenuOption> getOptions() {
			[]
		}

		@Override
		void run() {
			runs++
		}
	}

	def "main menu should list any submenus it receives and run the chosen one"() {
		given:
		ConsoleInput input = new ConsoleInput(new Scanner('2\n0\n'), out)
		ConsoleView view = new ConsoleView(out)
		FakeSubmenu reports = new FakeSubmenu('Relatórios', input, view)
		FakeSubmenu matches = new FakeSubmenu('Matches', input, view)

		when:
		new MainMenu(input, view, [reports, matches]).run()

		then:
		String printed = output.toString('UTF-8')
		printed.contains('1. Relatórios')
		printed.contains('2. Matches')
		printed.contains('Saindo do programa...')
		reports.runs == 0
		matches.runs == 1
	}
}
