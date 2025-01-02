package org.example.player

import io.netty.channel.ChannelHandlerContext
import org.example.base.net.AttributeKeys

object PlayerManager {
    private val playerMap = hashMapOf<Long, Player>()

    fun find(pid: Long) : Player? {
        return playerMap[pid]
    }

    fun onLogin(id: Long, ctx: ChannelHandlerContext) {
        if (playerMap.containsKey(id)) {
            val plr = playerMap.remove(id)
            if (plr != null) {
                plr.onLogout()
                plr.context.channel().attr(AttributeKeys.PLAYER_ID).set(null)
                plr.context.close()
            }
        }

        ctx.channel().attr(AttributeKeys.PLAYER_ID).set(id)
        val plr = Player(ctx)
        playerMap[id] = plr

        plr.onLogin()
    }

    fun onLogout(id: Long) {
        val plr = playerMap.remove(id)
        plr?.onLogout()
    }
}
