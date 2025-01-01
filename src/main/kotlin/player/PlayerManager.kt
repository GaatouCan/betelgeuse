package org.example.player

import io.netty.channel.ChannelHandlerContext
import org.example.net.AttributeKeys

object PlayerManager {

    fun find(pid: Long) : Player? {
        return Player()
    }

    fun onLogin(id: Long, ctx: ChannelHandlerContext) {
        ctx.channel().attr(AttributeKeys.PLAYER_ID).set(id)
    }
}
