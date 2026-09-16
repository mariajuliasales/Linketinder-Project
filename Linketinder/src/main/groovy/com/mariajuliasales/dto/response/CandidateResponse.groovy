package com.mariajuliasales.dto.response

record CandidateResponse(
        String name,
        String email,
        String state,
        String cep,
        String description,
        List<String> competences,
        String cpf,
        int age
) {
    @Override
    String toString() {
        return "Perfil do Candidato: " +
                "Nome: ${name} | Email: ${email} | Estado: ${state} | CEP: ${cep} | Descrição pessoal: ${description} | Competências: ${competences.join(', ')} | CPF: ${cpf} | Idade: ${age}"
    }
}