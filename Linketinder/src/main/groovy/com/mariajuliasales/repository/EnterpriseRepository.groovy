package com.mariajuliasales.repository

import com.mariajuliasales.model.Enterprise

interface EnterpriseRepository extends PersonRepository<Enterprise> {

	Enterprise findByCnpj(String cnpj)
}
