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
}
