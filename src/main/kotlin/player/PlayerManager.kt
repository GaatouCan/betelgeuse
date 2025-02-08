package org.example.player

import io.netty.channel.ChannelHandlerContext
import org.example.base.net.AttributeKeys
import org.example.base.net.Package
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import java.util.Calendar

object PlayerManager {
    private val playerMap: ConcurrentHashMap<Long, Player> = ConcurrentHashMap()

    init {
        val task = {
            playerMap.values.forEach {
                it.onDayChange()
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            val now = Calendar.getInstance()
            val nextRun = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)


                if (time.before(now.time)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            while (isActive) {
                val delayTime = nextRun.timeInMillis - Calendar.getInstance().timeInMillis
                delay(delayTime)
                task()

                nextRun.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    fun find(pid: Long) : Player? {
        return playerMap[pid]
    }

    fun onLogin(id: Long, ctx: ChannelHandlerContext) {
        if (playerMap.containsKey(id)) {
            val plr = playerMap.remove(id)
            if (plr != null) {
                plr.onLogout()
                plr.context.channel().attr(AttributeKeys.PLAYER_ID).set(null)
                plr.context.close()
            }
        }

        ctx.channel().attr(AttributeKeys.PLAYER_ID).set(id)
        val plr = Player(ctx)
        playerMap[id] = plr

        plr.onLogin()
    }

    fun onLogout(id: Long) {
        val plr = playerMap.remove(id)
        plr?.onLogout()
    }

    fun broadCast(pkg: Package, except: Set<Long>) {
        playerMap.forEach { (id, player) ->
            if (except.contains(id)) return@forEach
            player.sendPackage(pkg)
        }
    }
}
