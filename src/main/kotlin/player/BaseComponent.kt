package org.example.player

abstract class BaseComponent(open val owner: Player) {
    fun getPlayerID() : Long {
        return owner.getPlayerID()
    }

    open suspend fun deserialize() {}
    open suspend fun serialize() {}

    open fun onDayChange() {}
}