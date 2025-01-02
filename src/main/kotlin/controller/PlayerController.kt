package org.example.controller


import io.netty.channel.ChannelHandlerContext

import org.apache.logging.log4j.LogManager

import org.example.base.LoginManager
import org.example.base.route.ProtocolController
import org.example.base.route.RouteHandler
import org.example.base.route.RouteController
import org.example.base.net.AttributeKeys
import org.example.player.Player
import org.example.player.PlayerManager

import proto.user.User

@RouteController("player")
class PlayerController : ProtocolController {

    private val logger = LogManager.getLogger(PlayerController::class.java)

    @RouteHandler(1001)
    fun loginRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        val req = User.ClientLoginRequest.parseFrom(data)

        val pid = LoginManager.onLogin(req.id, req.token)
        if (pid <= 1000) return

        ctx.channel().attr(AttributeKeys.PLAYER_ID).set(pid)
        PlayerManager.onLogin(pid, ctx)

        logger.info("Player[$pid] logged in, token=${req.token}")
    }
}