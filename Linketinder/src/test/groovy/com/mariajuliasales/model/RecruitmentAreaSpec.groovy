package com.mariajuliasales.model

import spock.lang.Specification
import spock.lang.Unroll

class RecruitmentAreaSpec extends Specification {

	def "should have the same seven areas as the recruitment_area table"() {
		expect:
		RecruitmentArea.values()*.displayName == ['TI', 'RH', 'Marketing', 'Segurança da Informação', 'Financeiro',
			'Comercial', 'Operações']
	}

	@Unroll
	def "fromName should find '#name' ignoring case"() {
		expect:
		RecruitmentArea.fromName(name) == expected

		where:
		name                      || expected
		'TI'                      || RecruitmentArea.IT
		'ti'                      || RecruitmentArea.IT
		'rh'                      || RecruitmentArea.HR
		'SEGURANÇA DA INFORMAÇÃO' || RecruitmentArea.INFORMATION_SECURITY
		'operações'               || RecruitmentArea.OPERATIONS
		'  Marketing  '           || RecruitmentArea.MARKETING
	}

	@Unroll
	def "fromName should return null for '#name'"() {
		expect:
		RecruitmentArea.fromName(name) == null

		where:
		name << ['Jurídico', 'IT', 'INFORMATION_SECURITY', '', '   ', null]
	}

	def "toString should return the display name"() {
		expect:
		RecruitmentArea.INFORMATION_SECURITY.toString() == 'Segurança da Informação'
	}
}
