package org.example.player

abstract class BaseComponent(open val owner: Player) {
    fun getOwner(): Player {
        return owner
    }

    fun getPlayerID() : Long {
        return owner.getPlayerID()
    }
}