package org.example.repository

import org.example.base.database.BaseRepository
import org.example.table.AvatarTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and

object AvatarRepository : BaseRepository<AvatarTable>() {
    override val table = AvatarTable

    suspend fun findByIndex(pid: Long, idx: Int): ResultRow? {
        return find { (table.playerId eq pid) and (table.index eq idx) }
    }

    suspend fun findAllAvatar(pid: Long): List<ResultRow> {
        return findAll { (table.playerId eq pid) }
    }
}