package org.example.player.component

import org.example.base.repository.AvatarRepository
import org.example.base.table.AvatarTable
import org.example.player.BaseComponent
import org.example.player.Player
import org.example.player.PlayerComponent


@PlayerComponent("avatar")
class AvatarCT(override val owner: Player) : BaseComponent(owner) {

    private var curAvatar: Int = 0

    private val list = mutableListOf<Avatar>()

    override suspend fun deserialize() {
        AvatarRepository.findAllAvatar(getPlayerID()).forEach { row ->
            val avatar = Avatar(
                pid = row[AvatarTable.playerId],
                index = row[AvatarTable.avatarIndex],
                activated = row[AvatarTable.isActivated],
                inUsed = row[AvatarTable.isInUse]
            )

            list.add(avatar)

            if (avatar.inUsed)
                curAvatar = avatar.index
        }
    }

    override suspend fun serialize() {
        list.forEach { elem ->
            AvatarRepository.upsert {
                it[playerId] = elem.pid
                it[avatarIndex] = elem.index
                it[isActivated] = elem.activated
                it[isInUse] = elem.inUsed
            }
        }
    }

    override fun onDayChange() {

    }
}