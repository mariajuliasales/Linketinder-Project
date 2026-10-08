package com.mariajuliasales.dto.response

record CandidateResponse(
        int id,
        String name,
        String email,
        AddressResponse address,
        String description,
        List<String> competences,
        String cpf,
        int age,
        String training
) {
    @Override
    String toString() {
        return "Perfil do Candidato ${id}: " +
                "Nome: ${name} | Email: ${email} | CPF: ${cpf} | Idade: ${age} | Formação: ${training} | " +
                "Endereço: ${address} | Descrição pessoal: ${description} | " +
                "Competências: ${competences.join(', ')}"
    }
}