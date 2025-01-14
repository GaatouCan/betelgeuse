package org.example.controller

import io.netty.channel.ChannelHandlerContext
import org.apache.logging.log4j.LogManager
import org.example.base.route.ProtoMapping
import org.example.base.route.RouteMapping
import org.example.base.route.RouteController
import org.example.manager.friend.FriendManager
import org.example.player.Player
import proto.friend.Friend
import proto.friend.Friend.FriendApplyRequest
import proto.friend.Friend.FriendRequest


@RouteMapping("friend")
class FriendController : RouteController {

    private val logger = LogManager.getLogger(FriendController::class.java)

    @ProtoMapping(ProtocolType.FRIEND_REQUEST)
    fun onFriendListRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = FriendRequest.parseFrom(data)

        when (req.op) {
            1 -> FriendManager.sendFriendList(plr.getPlayerID(), 0)
            2 -> FriendManager.sendFriendList(plr.getPlayerID(), req.pid)
            3 -> FriendManager.removeFriend(plr.getPlayerID(), req.pid)
        }
    }

    @ProtoMapping(ProtocolType.FRIEND_APPLY_REQUEST)
    fun onApplyListRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = FriendApplyRequest.parseFrom(data)

        when (req.op) {
            1 -> FriendManager.sendApplyList(plr.getPlayerID(), 0)
            2 -> FriendManager.sendFriendList(plr.getPlayerID(), req.pid)
            3 -> FriendManager.removeApply(plr.getPlayerID(), req.pid)
        }
    }


    fun onAppliedRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = Friend.FriendAppliedRequest.parseFrom(data)

        when (req.op) {
            1 -> FriendManager.sendAppliedList(plr.getPlayerID(), 0)
            2 -> FriendManager.sendAppliedList(plr.getPlayerID(), req.pid)
            3 -> FriendManager.removeApplied(plr.getPlayerID(), req.pid)
        }
    }
}