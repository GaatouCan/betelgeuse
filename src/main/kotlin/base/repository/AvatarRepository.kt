package org.example.base.repository

import org.example.base.database.BaseRepository
import org.example.base.table.AvatarTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and

object AvatarRepository : BaseRepository<AvatarTable>() {
    override val table = AvatarTable

    suspend fun findByIndex(pid: Long, idx: Int): ResultRow? {
        return find { (AvatarTable.playerId eq pid) and (AvatarTable.avatarIndex eq idx) }
    }

    suspend fun findAllAvatar(pid: Long): List<ResultRow> {
        return findAll { (AvatarTable.playerId eq pid) }
    }
}