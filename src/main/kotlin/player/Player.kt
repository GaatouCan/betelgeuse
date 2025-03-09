package org.example.player

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

import org.example.base.net.AttributeKeys
import org.example.base.net.Package
import org.example.controller.ProtocolType
import org.reflections.scanners.Scanners

import proto.player.playerInfo


class Player(val context: ChannelHandlerContext) {

    companion object {
        const val AVAILABLE_PLAYER_ID: Int = 1000
    }

    private val logger = LogManager.getLogger(Player::class.java)

    val componentMap = hashMapOf<KClass<out BaseComponent>, BaseComponent>()
    private val nameToComponent = hashMapOf<String, BaseComponent>()

    init {
        val reflections = Reflections(ConfigurationBuilder().forPackages("org.example.player.component").addScanners(Scanners.SubTypes))
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

    fun onDayChange() {
        componentMap.values.forEach {
            it.onDayChange()
        }
    }

    fun getPlayerID(): Long {
        return context.channel().attr(AttributeKeys.PLAYER_ID).get() ?: 0
    }

    fun onLogin() {
        logger.info("Player[${getPlayerID()}] has logged in")
        val res = playerInfo {
            id = getPlayerID()
            name = "Gaatou"
            email = "Gaatou@gmail.com"
        }
        send(ProtocolType.PLAYER_INFO, res.toByteArray())

        // 协程中读取数据库加载
        CoroutineScope(Dispatchers.IO).launch {
            componentMap.forEach { (_, ct) ->
                ct.deserialize()
            }
        }
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
        val pkg = Package.newInstance(type.value, data)
        sendPackage(pkg)
    }

    fun sendPackage(pkg: Package) {
        // 发送信息放在协程里
        CoroutineScope(Dispatchers.IO).launch {
            context.writeAndFlush(pkg)
        }
    }
}
