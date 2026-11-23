package com.mariajuliasales.model

/**
 * Vaga curtida com nome e e-mail da empresa, que só são preenchidos quando vacancy.matched() é verdadeiro
 * (null sem match). Sai do service no lugar da entidade Enterprise, que carrega o CNPJ. Nunca é serializado.
 */
record LikedVacancyView(LikedVacancy vacancy, String enterpriseName, String enterpriseEmail) {
}
