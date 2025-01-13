package org.example.player

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.example.base.net.AttributeKeys
import org.example.base.net.Package
import org.example.controller.ProtocolType
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

class Player(val context: ChannelHandlerContext) {

    private val logger = LogManager.getLogger(Player::class.java)

    val componentMap = hashMapOf<KClass<out BaseComponent>, BaseComponent>()
    private val nameToComponent = hashMapOf<String, BaseComponent>()

    init {
        val reflections = Reflections(ConfigurationBuilder().forPackages("org.example.player.component").addScanners(SubTypesScanner(false)))
        val classes = reflections.getSubTypesOf(BaseComponent::class.java).map { it.kotlin }

        classes.forEach { clazz ->
            val annotation = clazz.findAnnotation<PlayerComponent>()
            if (annotation != null) {
                try {
                    clazz.primaryConstructor?.let { constructor ->
                        val ct = constructor.call(this)
                        componentMap[clazz] = ct
                        nameToComponent[annotation.name] = ct
                    }
                } catch (e: Exception) {
                    logger.error("Init Player[${getPlayerID()}] error: ${e.message}")
                }
            }
        }
    }

    fun getPlayerID(): Long {
        return context.channel().attr(AttributeKeys.PLAYER_ID).get() ?: 0
    }

    fun onLogin() {
        logger.info("Player[${getPlayerID()}] has logged in")
    }

    fun onLogout() {
        logger.info("Player[${getPlayerID()}] has logged out")
    }

    inline fun <reified T: BaseComponent> getComponent(componentClass: KClass<T>) : T? {
        return componentMap[componentClass]?.let {
            it as? T
        }
    }

    fun getComponentByName(name: String): BaseComponent? {
        return nameToComponent[name]
    }

    fun send(type: ProtocolType, data: ByteArray) {
        val pkg = Package.createPackage(type.value, data)
        context.writeAndFlush(pkg)
    }

    fun sendPackage(pkg: Package) {
        context.writeAndFlush(pkg)
    }
}
