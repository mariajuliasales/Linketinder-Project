package com.mariajuliasales.dao

interface ConnectionManager {

	def <T> T withConnection(Closure<T> work)

	def <T> T withTransaction(Closure<T> work)
}