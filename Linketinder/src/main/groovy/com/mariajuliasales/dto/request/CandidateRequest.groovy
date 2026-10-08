package com.mariajuliasales.dto.request

import java.time.LocalDate


record CandidateRequest(
        String name,
        String email,
        String password,
        AddressRequest address,
        String description,
        List<String> competences,
        String cpf,
        LocalDate birthDate,
        String training
) {
}