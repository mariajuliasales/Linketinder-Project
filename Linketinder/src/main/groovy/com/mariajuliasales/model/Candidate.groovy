package com.mariajuliasales.model

import java.time.LocalDate
import java.time.Period

class Candidate extends PersonAbstract{

    String cpf
    LocalDate birthDate
    String training

    int getAge() {
        birthDate ? Period.between(birthDate, LocalDate.now()).years : 0
    }

    @Override
    String viewProfileAnonymous() {
        "Perfil do Candidato ${id}: " +
                "Formação: ${training} | Descrição pessoal: ${description} | Competências: ${competences.join(', ')}"
    }

}
