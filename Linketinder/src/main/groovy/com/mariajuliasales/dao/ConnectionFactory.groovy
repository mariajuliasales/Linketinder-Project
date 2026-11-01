package com.mariajuliasales.dao

import java.sql.Connection

interface ConnectionFactory {

	Connection createConnection()
}
