package org.example.base.net

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.apache.logging.log4j.LogManager
import java.io.IOException

import org.example.base.route.ProtocolRoute
import org.example.player.PlayerManager

class ServerHandler : SimpleChannelInboundHandler<Package>() {
    private val logger = LogManager.getLogger(ServerHandler::class.java)

    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)

        if (ctx == null) return
        logger.info("Connected to ${ctx.channel().remoteAddress()}")
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, pkg: Package?) {
        if (pkg == null || ctx == null) return

        ProtocolRoute.onPackage(ctx, pkg)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        if (ctx == null) return

        val pid = ctx.channel().attr(AttributeKeys.PLAYER_ID).get() ?: return
        logger.info("Player [$pid] Disconnected.")

        PlayerManager.onLogout(pid)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        if (ctx == null || cause == null) return
        if (cause is IOException) {
            logger.warn("Client${ctx.channel().remoteAddress()} Connection Closed.")
        } else {
            cause.printStackTrace()
        }
        ctx.close()
    }
}