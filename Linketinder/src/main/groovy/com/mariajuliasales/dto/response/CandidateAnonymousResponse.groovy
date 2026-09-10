package com.mariajuliasales.dto.response

record CandidateAnonymousResponse(
        String state,
        String cep,
        String description,
        List<String> competences
) {

    @Override
    String toString() {
        return "Perfil do Candidato: " +
                "CEP: ${cep} | Estado: ${state} | Descrição pessoal: ${description} | Competências: ${competences.join(', ')}"
    }
}