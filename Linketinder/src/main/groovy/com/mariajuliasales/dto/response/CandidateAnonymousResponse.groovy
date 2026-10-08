package com.mariajuliasales.dto.response

record CandidateAnonymousResponse(
        int id,
        String training,
        String description,
        List<String> competences
) {

    @Override
    String toString() {
        return "Perfil do Candidato ${id}: " +
                "Formação: ${training} | Descrição pessoal: ${description} | Competências: ${competences.join(', ')}"
    }
}
