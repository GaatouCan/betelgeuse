package org.example.controller

import io.netty.channel.ChannelHandlerContext
import org.example.base.route.ProtoMapping
import org.example.base.route.RouteController
import org.example.base.route.RouteMapping
import org.example.manager.friend.FriendManager
import org.example.player.Player
import proto.friend.Friend
import proto.friend.Friend.FriendApplyRequest
import proto.friend.Friend.FriendRequest
import proto.friend.friendAppliedResult
import proto.friend.sendApplyResponse


@RouteMapping("friend")
class FriendController : RouteController {

    // private val logger = LogManager.getLogger(FriendController::class.java)

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
            4 -> FriendManager.cleanAcceptedApply(plr.getPlayerID())
            5 -> FriendManager.cleanRejectedApply(plr.getPlayerID())
            6 -> FriendManager.cleanApply(plr.getPlayerID())
        }
    }

    @ProtoMapping(ProtocolType.FRIEND_APPLIED_REQUEST)
    fun onAppliedRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = Friend.FriendAppliedRequest.parseFrom(data)

        when (req.op) {
            1 -> FriendManager.sendAppliedList(plr.getPlayerID(), 0)
            2 -> FriendManager.sendAppliedList(plr.getPlayerID(), req.pid)
            3 -> FriendManager.removeApplied(plr.getPlayerID(), req.pid)
            4 -> FriendManager.cleanAcceptedApplied(plr.getPlayerID())
            5 -> FriendManager.cleanRejectedApplied(plr.getPlayerID())
            6 -> FriendManager.cleanApplied(plr.getPlayerID())
        }
    }

    @ProtoMapping(ProtocolType.SEND_APPLY_REQUEST)
    fun onSendFriendApplyRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = Friend.SendApplyRequest.parseFrom(data)

        val ret = FriendManager.sendFriendApply(plr.getPlayerID(), req.pid)
        val res = sendApplyResponse {
            pid = req.pid
            result = ret
        }
        plr.send(ProtocolType.SEND_APPLY_RESPONSE, res.toByteArray())

        when (ret) {
            0 -> {
                FriendManager.sendApplyList(plr.getPlayerID(), req.pid)
                FriendManager.sendAppliedList(req.pid, plr.getPlayerID())
            }
            2 -> FriendManager.sendBlackList(plr.getPlayerID(), req.pid)
        }
    }

    @ProtoMapping(ProtocolType.FRIEND_APPLIED_HANDLE)
    fun onHandleApplied(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = Friend.FriendAppliedHandle.parseFrom(data)

        when (req.op) {
            1 -> {
                val ret = FriendManager.acceptApply(plr.getPlayerID(), req.pid)
                val res = friendAppliedResult {
                    pid = req.pid
                    result = ret
                }
                plr.send(ProtocolType.FRIEND_APPLIED_RESULT, res.toByteArray())
            }
            2 -> {
                FriendManager.rejectApply(plr.getPlayerID(), req.pid)
                val res = friendAppliedResult {
                    pid = req.pid
                    result = 1
                }
                plr.send(ProtocolType.FRIEND_APPLIED_RESULT, res.toByteArray())
            }
        }

        FriendManager.sendAppliedList(plr.getPlayerID(), req.pid)
        FriendManager.sendApplyList(req.pid, plr.getPlayerID())
    }

    @ProtoMapping(ProtocolType.BLACK_LIST_REQUEST)
    fun onBlacklistRequest(data: ByteArray, ctx: ChannelHandlerContext, plr: Player?) {
        if (plr == null) return
        val req = Friend.BlackListRequest.parseFrom(data)

        when (req.op) {
            1 -> FriendManager.sendBlackList(plr.getPlayerID(), 0)
            2 -> FriendManager.sendBlackList(plr.getPlayerID(), req.pid)
            3 -> FriendManager.addToBlackList(plr.getPlayerID(), req.pid)
            4 -> FriendManager.removeFromBlackList(plr.getPlayerID(), req.pid)
            5 -> FriendManager.cleanBlackList(plr.getPlayerID())
        }
    }
}