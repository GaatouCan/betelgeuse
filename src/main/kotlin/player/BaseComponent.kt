package org.example.player

abstract class BaseComponent(open val owner: Player) {
    fun getPlayerID() : Long {
        return owner.getPlayerID()
    }

    abstract suspend fun init()

    open fun onDayChange() {}
}