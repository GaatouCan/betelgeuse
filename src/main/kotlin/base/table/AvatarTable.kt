package org.example.base.table

import org.jetbrains.exposed.sql.Table

object AvatarTable : Table() {
    var playerId = long("player_id")
    var avatarIndex = integer("avatar_idx")
    val isActivated = bool("is_activated").default(false)
    val isInUse = bool("is_in_use").default(false)

    override val primaryKey = PrimaryKey(playerId, avatarIndex)
}