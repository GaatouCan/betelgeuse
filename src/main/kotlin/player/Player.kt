package org.example.player

import io.netty.channel.ChannelHandlerContext
import org.example.base.net.AttributeKeys

class Player(val context: ChannelHandlerContext) {

    fun getPlayerID(): Long {
        return context.channel().attr(AttributeKeys.PLAYER_ID).get() ?: 0
    }

    fun onLogin() {
        // TODO
    }

    fun onLogout() {
        // TODO
    }
}