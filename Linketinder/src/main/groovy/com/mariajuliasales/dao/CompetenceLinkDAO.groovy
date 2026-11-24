package com.mariajuliasales.dao

import com.mariajuliasales.model.Competence

import java.sql.Connection
import java.sql.ResultSet

class CompetenceLinkDAO {

	private final CompetenceDAO competenceDAO

	CompetenceLinkDAO(CompetenceDAO competenceDAO) {
		this.competenceDAO = competenceDAO
	}

	void replaceLinks(Connection connection, CompetenceLink link, int ownerId, List<Competence> competences) {
		SqlExecutor.update(connection, "DELETE FROM ${link.table} WHERE ${link.ownerColumn} = ?", [ownerId])

		String insertLink = "INSERT INTO ${link.table} (${link.ownerColumn}, competence_id) VALUES (?, ?) " +
				'ON CONFLICT DO NOTHING'
		competences.findAll { it?.name }.unique().each { Competence competence ->
			competence.id = competenceDAO.findOrCreate(connection, competence.name).id
			SqlExecutor.update(connection, insertLink, [ownerId, competence.id])
		}
	}

	List<Competence> findLinked(Connection connection, CompetenceLink link, int ownerId) {
		String sql = """
                SELECT co.id, co.name
                FROM ${link.table} owner_link
                JOIN competence co ON co.id = owner_link.competence_id
                WHERE owner_link.${link.ownerColumn} = ?
                ORDER BY co.name"""
		SqlExecutor.query(connection, sql, [ownerId]) { ResultSet row -> CompetenceDAO.mapRow(row) }
	}

	/**
	 * Competências de vários donos numa única consulta, ordenadas por nome. Um ? por id (nunca concatena valores).
	 * Donos sem competência não aparecem no mapa; sem ids, não consulta o banco.
	 */
	Map<Integer, List<Competence>> findLinkedByOwners(Connection connection, CompetenceLink link, Collection<Integer> ownerIds) {
		if (!ownerIds) {
			return [:]
		}
		List<Integer> distinctIds = ownerIds.toUnique()
		String placeholders = (['?'] * distinctIds.size()).join(', ')
		String sql = """
                SELECT owner_link.${link.ownerColumn} AS owner_id, co.id, co.name
                FROM ${link.table} owner_link
                JOIN competence co ON co.id = owner_link.competence_id
                WHERE owner_link.${link.ownerColumn} IN (${placeholders})
                ORDER BY owner_link.${link.ownerColumn}, co.name"""
		Map<Integer, List<Competence>> competencesByOwner = [:]
		SqlExecutor.query(connection, sql, distinctIds) { ResultSet row ->
			competencesByOwner.computeIfAbsent(row.getInt('owner_id')) { [] } << CompetenceDAO.mapRow(row)
		}
		competencesByOwner
	}
}
