package com.mariajuliasales.model

class Candidate extends PersonAbstract{

    String cpf
    int age

    Candidate(int id, String name, String email, String state, String cep, String description, List<Competence> competences, String cpf, int age) {
        super(id, name, email, state, cep, description, competences)
        this.cpf = cpf
        this.age = age
    }

    @Override
    String viewProfileAnonymous() {
        println "Perfil do Candidato ${id}: " +
                "Estado: ${state} | CEP: ${cep} | Descrição pessoal: ${description} | Competências: ${competences.join(', ')}"
    }

}
