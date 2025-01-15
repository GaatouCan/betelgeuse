package org.example.controller


import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.example.base.LoginManager
import org.example.base.net.AttributeKeys
import org.example.base.route.ProtoMapping
import org.example.base.route.RouteController
import org.example.base.route.RouteMapping
import org.example.player.Player
import org.example.player.PlayerManager

@RouteMapping("player")
class PlayerController : RouteController {

    private val logger = LogManager.getLogger(PlayerController::class.java)

    @ProtoMapping(ProtocolType.CLIENT_LOGIN_REQUEST)
    fun loginRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        // 已经设置了PLAYER_ID的忽略
        if (ctx.channel().hasAttr(AttributeKeys.PLAYER_ID))
            return

        val req = proto.player.Player.ClientLoginRequest.parseFrom(data)

        val pid = LoginManager.onLogin(req.id, req.token)
        if (pid <= 1000) return

        logger.info("Player[$pid] login request, token=${req.token}")

        ctx.channel().attr(AttributeKeys.PLAYER_ID).set(pid)
        PlayerManager.onLogin(pid, ctx)
    }
}