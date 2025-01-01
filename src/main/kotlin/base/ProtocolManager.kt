package org.example.base

import io.netty.channel.ChannelHandlerContext

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder

import org.example.net.AttributeKeys
import org.example.net.Package
import org.example.player.Player
import org.example.player.PlayerManager


object ProtocolManager {
    private val handlerMap : HashMap<Int, (ByteArray, ChannelHandlerContext, Player?) -> Unit> = hashMapOf()
    private val logger: Logger = LogManager.getLogger(ProtocolManager::class.java)

    init {
        val reflections = Reflections(ConfigurationBuilder().forPackages("").addScanners(SubTypesScanner(false)))
        val controllers = reflections.getSubTypesOf(ProtocolController::class.java)

        controllers.forEach { clazz ->
            if (clazz.isAnnotationPresent(RouteController::class.java)) {
                try {
                    val controller = clazz.getDeclaredConstructor().newInstance()
                    val methods = clazz.methods

                    methods.forEach { method ->
                        val annotation = method.getAnnotation(RouteHandler::class.java)
                        if (annotation != null) {
                            handlerMap[annotation.handler] = { data, context, plr ->
                                method.invoke(controller, data, context, plr)
                            }
                        }
                    }
                } catch (e : Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

//    fun showAll() {
//        handlerMap.forEach { (key, handler) ->
//            logger.info(key)
//        }
//    }
//
//    fun playerInvoke(id: Int, plr: Player, data: ByteArray) {
//        val handler = handlerMap[id]?: return
//        handler.invoke(plr, data)
//    }

    fun onPackage(ctx : ChannelHandlerContext, pkg: Package) {
        val handler = handlerMap[pkg.header.id]
        if (handler == null) {
            logger.error("Cannot find handler for ${pkg.header.id}")
            return
        }

        if (ctx.channel().hasAttr(AttributeKeys.PLAYER_ID)) {
            val pid = ctx.channel().attr(AttributeKeys.PLAYER_ID).get()
            val plr = PlayerManager.find(pid)
            handler(pkg.data, ctx, plr)

            return
        }

        if (pkg.header.id != 1001) return

        handler(pkg.data, ctx, null)
    }
}