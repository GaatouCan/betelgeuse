package org.example.player.component

import org.example.player.BaseComponent
import org.example.player.Player
import org.example.player.PlayerComponent

@PlayerComponent("avatar")
class AvatarCT(override val owner: Player) : BaseComponent(owner) {
    private val curAvatar: Int = 0

    override fun onDayChange() {

    }
}