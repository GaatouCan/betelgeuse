package org.example

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

import org.example.base.net.PackageCodec
import org.example.base.net.ServerHandler
import org.apache.logging.log4j.LogManager
import org.example.base.route.ProtocolRoute

fun main() {
    ProtocolRoute.showAllRoute()

    val bossGroup: EventLoopGroup = NioEventLoopGroup()
    val workerGroup: EventLoopGroup = NioEventLoopGroup()
    val logger = LogManager.getLogger("MainLogger")

    try {
        val bootstrap = ServerBootstrap()
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(PackageCodec())
                        .addLast(ServerHandler())
                }
            })

        val channelFuture = bootstrap.bind(8080).sync()
        logger.info("Listening on port 8080")

        channelFuture.channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}