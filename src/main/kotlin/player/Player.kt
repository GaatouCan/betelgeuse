package org.example.player

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.example.base.net.AttributeKeys

class Player(val context: ChannelHandlerContext) {

    private val logger = LogManager.getLogger(Player::class.java)

    fun getPlayerID(): Long {
        return context.channel().attr(AttributeKeys.PLAYER_ID).get() ?: 0
    }

    fun onLogin() {
        logger.info("Player[${getPlayerID()}] has logged in")
    }

    fun onLogout() {
        logger.info("Player[${getPlayerID()}] has logged out")
    }
}