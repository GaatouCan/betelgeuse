package org.example.player.component

import org.example.base.repository.AvatarRepository
import org.example.player.BaseComponent
import org.example.player.Player
import org.example.player.PlayerComponent


@PlayerComponent("avatar")
class AvatarCT(override val owner: Player) : BaseComponent(owner) {

    private val curAvatar: Int = 0

    override suspend fun deserialize() {
        AvatarRepository.findAllAvatar(getPlayerID()).forEach { row ->
            // TODO
        }
    }

    override suspend fun serialize() {
        // TODO
    }

    override fun onDayChange() {

    }
}