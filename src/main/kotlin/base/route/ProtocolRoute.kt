package org.example.base.route

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.base.net.AttributeKeys
import org.example.base.net.Package
import org.example.player.Player
import org.example.player.PlayerManager
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess


object ProtocolRoute {
    private val handlerMap : HashMap<Int, (ByteArray, ChannelHandlerContext, Player?) -> Unit> = hashMapOf()
    private val controllerMap : HashMap<String, RouteController> = hashMapOf()

    private val logger: Logger = LogManager.getLogger(ProtocolRoute::class.java)

    init {
        val reflections = Reflections(ConfigurationBuilder().forPackages("org.example.controller").addScanners(SubTypesScanner(false)))
        val classes = reflections.getSubTypesOf(RouteController::class.java).map { it.kotlin }

//        val reflections = Reflections(ConfigurationBuilder().forPackages("org.example").addScanners(TypeAnnotationsScanner()))
//        val classes = reflections.getTypesAnnotatedWith(RouteController::class.java)

        classes.forEach { clazz ->
            val routeAnnotation = clazz.findAnnotation<RouteMapping>()
            if (routeAnnotation != null) {
                try {
                    val controller = clazz.primaryConstructor?.call() ?: return@forEach

                    controllerMap[routeAnnotation.route] = controller

                    clazz.memberFunctions.forEach { method ->
                        val protoAnnotation = method.findAnnotation<ProtoMapping>()
                        if (protoAnnotation != null) {
                            handlerMap[protoAnnotation.type.value] = { data, context, plr ->
                                method.call(controller, data, context, plr)
                            }
                        }
                    }
                } catch (e : Exception) {
                    e.printStackTrace()
                    exitProcess(1)
                }
            }
        }
    }

    fun showAllRoute() {
        controllerMap.forEach { (key, _) ->
            logger.info("Load Protocol Route $key")
        }
    }

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