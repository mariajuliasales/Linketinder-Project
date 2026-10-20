package com.mariajuliasales.model

import spock.lang.Specification

class CompetenceSpec extends Specification {

	def "competences with the same name in different cases should be equal"() {
		expect:
		new Competence("Java") == new Competence("java")
		[
			new Competence("Java"),
			new Competence("JAVA")
		].unique().size() == 1
	}

	def "competence name should be trimmed"() {
		expect:
		new Competence("  Groovy  ").name == "Groovy"
	}
}
