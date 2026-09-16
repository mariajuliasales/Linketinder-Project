package com.mariajuliasales.dto.request

record CandidateRequest(
        int id,
        String name,
        String email,
        String state,
        String cep,
        String description,
        List<String> competences,
        String cpf,
        int age
) {
}