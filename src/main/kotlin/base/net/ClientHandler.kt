package org.example.base.net

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ClientHandler : SimpleChannelInboundHandler<Package>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, pkg: Package?) {
        if (pkg == null || ctx == null) return
        println(pkg.data.toString(Charsets.UTF_8))
    }
}