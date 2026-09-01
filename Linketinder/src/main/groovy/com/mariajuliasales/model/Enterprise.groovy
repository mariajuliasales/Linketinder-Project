package com.mariajuliasales.model

class Enterprise extends PersonAbstract{

    String cnpj
    String country

    Enterprise(int id, String name, String email, String state, String cep, String description, List<Competence> competences, String cnpj, String country) {
        super(id, name, email, state, cep, description, competences)
        this.cnpj = cnpj
        this.country = country
    }

    @Override
    String viewProfileAnonymous() {
        println "Perfil da Empresa ${id}: " +
                "CEP: ${cep} | Estado: ${state} | Country: ${country} | Descrição da empresa: ${description} | Competences: ${competences.join(', ')}"
    }

}
