package org.example

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.example.base.net.ClientHandler
import org.example.base.net.Package
import org.example.base.net.PackageCodec
import proto.player.clientLoginRequest

fun main(args: Array<String>) {
    val host = "localhost"
    val port = 8080

    val workerGroup: EventLoopGroup = NioEventLoopGroup()
    try {
        val bootstrap = Bootstrap()
        bootstrap
            .group(workerGroup)
            .channel(NioSocketChannel::class.java)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(PackageCodec())
                        .addLast(ClientHandler())
                }
            })

        val future = bootstrap.connect(host, port).sync()

        val req = clientLoginRequest {
            id = 1000001
            token = "Trust Me"
        }
        val pkg = Package.buildPackage(1001, req.toByteArray())

        future.channel().writeAndFlush(pkg)

        future.channel().closeFuture().sync()
    } finally {
        workerGroup.shutdownGracefully()
    }
}