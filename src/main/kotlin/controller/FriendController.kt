package org.example.controller

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.example.base.route.ProtoMapping
import org.example.base.route.RouteController
import org.example.base.route.RouteMapping
import org.example.manager.friend.FriendManager
import org.example.player.Player
import proto.friend.Friend.FriendListRequest


@RouteMapping("friend")
class FriendController : RouteController {

    private val logger = LogManager.getLogger(FriendController::class.java)

    @ProtoMapping(ProtocolType.FRIEND_LIST_REQUEST)
    fun getFriendList(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = FriendListRequest.parseFrom(data)

        when (req.op) {
            1 -> {
                FriendManager.sendFriendList(plr.getPlayerID(), req.pid)
            }
        }
    }
}