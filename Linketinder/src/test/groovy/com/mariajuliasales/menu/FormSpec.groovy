package com.mariajuliasales.menu

import com.mariajuliasales.dto.request.AddressRequest
import com.mariajuliasales.model.Competence
import com.mariajuliasales.service.CompetenceCatalog
import spock.lang.Specification

import static com.mariajuliasales.fixture.TestData.validAddress

class FormSpec extends Specification {

	CompetenceCatalog catalog = Stub() {
		findAll() >> [
			new Competence(1, 'Java'),
			new Competence(2, 'Groovy')
		]
	}

	ByteArrayOutputStream output = new ByteArrayOutputStream()
	PrintStream out = new PrintStream(output, true, 'UTF-8')
	ConsoleView view = new ConsoleView(out)

	private ConsoleInput inputWith(String... lines) {
		new ConsoleInput(new Scanner(lines.join('\n') + '\n'), out)
	}

	def "address form should read every field in order"() {
		given:
		AddressForm form = new AddressForm(inputWith('01310-000', 'Rua A', '10', 'Apto 1', 'Centro', 'Recife', 'PE', 'Brasil'), view)

		expect:
		form.read('Endereço:', null) == new AddressRequest('01310-000', 'Rua A', '10', 'Apto 1', 'Centro', 'Recife', 'PE', 'Brasil')
	}

	def "address form should keep the current values when Enter is pressed"() {
		given:
		AddressForm form = new AddressForm(inputWith('', '', '', '', '', '', '', ''), view)

		when:
		AddressRequest request = form.read('Endereço:', validAddress())

		then:
		request.cep() == '01310-000'
		request.street() == 'Avenida Paulista'
		request.city() == 'São Paulo'
		request.country() == 'Brasil'
	}

	def "competence form should split, trim and remove repeated names ignoring case"() {
		given:
		CompetenceForm form = new CompetenceForm(inputWith(' Java, groovy , JAVA,, Kotlin '), view, catalog)

		expect:
		form.read('Competências:', [])*.name == ['Java', 'groovy', 'Kotlin']
	}

	def "competence form should keep the current competences when Enter is pressed"() {
		given:
		List<Competence> current = [new Competence('SQL')]
		CompetenceForm form = new CompetenceForm(inputWith(''), view, catalog)

		expect:
		form.read('Competências', current).is(current)
	}

	def "competence form should show the competences available in the catalog"() {
		given:
		CompetenceForm form = new CompetenceForm(inputWith('Java'), view, catalog)

		when:
		form.read('Competências:', [])

		then:
		output.toString('UTF-8').contains('Competências disponíveis: Java, Groovy')
	}
}
