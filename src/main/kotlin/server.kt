package org.example

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.apache.logging.log4j.LogManager
import org.example.base.config.ConfigManager
import org.example.base.net.PackageCodec
import org.example.base.net.ServerHandler
import org.example.base.route.ProtocolRoute
import kotlin.system.exitProcess

fun main() {
    val logger = LogManager.getLogger("MainLogger")

    val globalConfig = ConfigManager.getGlobalConfig()
    if (globalConfig == null) {
        logger.error("GlobalConfig is null")
        exitProcess(1)
    }

    ProtocolRoute.showAllRoute()

    val bossGroup: EventLoopGroup = NioEventLoopGroup()
    val workerGroup: EventLoopGroup = NioEventLoopGroup(globalConfig.server.worker)

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

        val channelFuture = bootstrap.bind(globalConfig.server.port).sync()
        logger.info("Listening on port ${globalConfig.server.port}")

        channelFuture.channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}