package com.mariajuliasales.mapper

import com.mariajuliasales.model.Competence
import spock.lang.Specification

class CompetenceMapperSpec extends Specification {

	def "names should be converted to competences"() {
		expect:
		CompetenceMapper.toEntities(["Java", " Groovy "]) == [
			new Competence("Java"),
			new Competence("Groovy")
		]
	}

	def "competences should be converted to names"() {
		expect:
		CompetenceMapper.toNames([
			new Competence(1, "Java"),
			new Competence(2, "SQL")
		]) == ["Java", "SQL"]
	}

	def "null lists should be converted to empty lists"() {
		expect:
		CompetenceMapper.toEntities(null) == []
		CompetenceMapper.toNames(null) == []
	}
}
