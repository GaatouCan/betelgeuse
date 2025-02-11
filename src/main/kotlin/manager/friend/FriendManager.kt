﻿package org.example.manager.friend

import org.example.controller.ProtocolType
import org.example.player.Player.Companion.AVAILABLE_PLAYER_ID
import org.example.player.PlayerManager
import proto.friend.*
import proto.friend.BlackListResponseKt.blackListInfo

object FriendManager {

    // 玩家好友信息二维哈希表
    private val friendMap = hashMapOf<Long, HashMap<Long, FriendInfo>>()

    private val applyMap = hashMapOf<Long, HashMap<Long, ApplyInfo>>()
    private val appliedMap = hashMapOf<Long, HashMap<Long, ApplyInfo>>()

    private val blackListMap = hashMapOf<Long, HashMap<Long, BlackInfo>>()

    init {

    }

    fun checkFriend(lhs: Long, rhs: Long): Boolean {
        if (lhs <= AVAILABLE_PLAYER_ID || rhs <= AVAILABLE_PLAYER_ID) return false
        if (lhs == rhs) return false

        friendMap[lhs]?.let { iter ->
            iter[rhs]?.let {
                return it.startTime > 0
            }
        }

        friendMap[rhs]?.let { iter ->
            iter[lhs]?.let {
                return it.startTime > 0
            }
        }

        return false
    }

    /**
     * @return 1 已经是好友; 2 拉黑了对面; 3 被对面拉黑; 0 成功
     */
    fun addFriend(lhs: Long, rhs: Long): Int {
        if (lhs <= AVAILABLE_PLAYER_ID || rhs <= AVAILABLE_PLAYER_ID) return -1
        if (lhs == rhs) return -1

        checkFriend(lhs, rhs).takeIf { it }?.let {
            return 1
        }

        when(checkBlackList(lhs, rhs)) {
            1 -> return 2
            2 -> return 3
        }

        val info = FriendInfo(lhs, rhs, System.currentTimeMillis())

        if (!friendMap.containsKey(lhs))
            friendMap[lhs] = hashMapOf()
        friendMap[lhs]!![rhs] = info

        if (!friendMap.containsKey(rhs))
            friendMap[rhs] = hashMapOf()
        friendMap[rhs]!![lhs] = info

        // TODO: 同步数据库

        return 0
    }

    fun sendFriendList(lhs: Long, rhs: Long) {
        val plr = PlayerManager.find(lhs) ?: return

        if (rhs > AVAILABLE_PLAYER_ID) {
            val res = friendResponse {
                sendAll = false
                friendMap[lhs]?.let { iter ->
                    iter[rhs]?.also {
                        list += friendInfo {
                            pid = rhs
                            startTime = it.startTime
                            tag = "null"
                        }
                    } ?: run {
                        list += friendInfo {
                            pid = rhs
                            startTime = -1
                            tag = "null"
                        }
                    }
                }
            }
            plr.send(ProtocolType.FRIEND_RESPONSE, res.toByteArray())
            return
        }

        val res = friendResponse {
            sendAll = true
            friendMap[lhs]?.let {
                it.forEach { info ->
                    list += friendInfo {
                        pid = info.key
                        startTime = info.value.startTime
                        tag = "null"
                    }
                }
            }
        }
        plr.send(ProtocolType.FRIEND_RESPONSE, res.toByteArray())
    }

    fun removeFriend(lhs: Long, rhs: Long) {
        checkFriend(lhs, rhs).takeUnless { it }?.let {
            return
        }

        friendMap[lhs]?.remove(rhs)
        friendMap[rhs]?.remove(lhs)

        sendFriendList(lhs, rhs)
        sendFriendList(rhs, lhs)
    }

    fun checkFriendApply(lhs: Long, rhs: Long): Boolean {
        if (lhs <= AVAILABLE_PLAYER_ID || rhs <= AVAILABLE_PLAYER_ID) return false
        if (lhs == rhs) return false

        applyMap[lhs]?.let { iter ->
            iter[rhs]?.let {
                return it.startTime > 0
            }
        }

        appliedMap[rhs]?.let { iter ->
            iter[lhs]?.let {
                return it.startTime > 0
            }
        }

        return false
    }

    fun sendApplyList(lhs: Long, rhs: Long) {
        val plr = PlayerManager.find(lhs) ?: return

        if (rhs >= AVAILABLE_PLAYER_ID) {
            val res = friendApplyResponse {
                sendAll = false
                applyMap[lhs]?.let { iter ->
                    iter[rhs]?.also {
                        list += applyInfo {
                            fromPlayer = it.fromPlayer
                            toPlayer = it.toPlayer
                            timestamp = it.startTime
                            state = it.state
                        }
                    } ?: run {
                        list += applyInfo {
                            fromPlayer = lhs
                            toPlayer = rhs
                            timestamp = -1
                            state = 0
                        }
                    }
                }
            }
            plr.send(ProtocolType.FRIEND_APPLY_RESPONSE, res.toByteArray())
            return
        }

        val res = friendApplyResponse {
            sendAll = true
            applyMap[lhs]?.let { iter ->
                iter.forEach { info ->
                    list += applyInfo {
                        fromPlayer = info.key
                        toPlayer = info.value.toPlayer
                        timestamp = info.value.startTime
                        state = info.value.state
                    }
                }
            }
        }
        plr.send(ProtocolType.FRIEND_APPLY_RESPONSE, res.toByteArray())
    }

    fun sendAppliedList(lhs: Long, rhs: Long) {
        val plr = PlayerManager.find(lhs) ?: return

        if (rhs >= AVAILABLE_PLAYER_ID) {
            val res = friendAppliedResponse {
                sendAll = false
                appliedMap[lhs]?.let { iter ->
                    iter[rhs]?.also {
                        list += applyInfo {
                            fromPlayer = it.fromPlayer
                            toPlayer = it.toPlayer
                            timestamp = it.startTime
                            state = it.state
                        }
                    } ?: run {
                        list += applyInfo {
                            fromPlayer = rhs
                            toPlayer = lhs
                            timestamp = -1
                            state = 0
                        }
                    }
                }
            }
            plr.send(ProtocolType.FRIEND_APPLIED_RESPONSE, res.toByteArray())
            return
        }

        val res = friendAppliedResponse {
            sendAll = true
            appliedMap[lhs]?.let { iter ->
                iter.forEach { info ->
                    list += applyInfo {
                        fromPlayer = info.key
                        toPlayer = info.value.toPlayer
                        timestamp = info.value.startTime
                        state = info.value.state
                    }
                }
            }
        }
        plr.send(ProtocolType.FRIEND_APPLIED_RESPONSE, res.toByteArray())
    }

    fun removeApply(lhs: Long, rhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        if (lhs == rhs) return

        applyMap[lhs]?.remove(rhs)
    }

    fun removeApplied(lhs: Long, rhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        if (lhs == rhs) return

        appliedMap[lhs]?.remove(rhs)
    }

    fun cleanApply(lhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        applyMap.remove(lhs)
    }

    fun cleanApplied(lhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        appliedMap.remove(lhs)
    }

    fun cleanAcceptedApply(lhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        applyMap[lhs]?.let { iter ->
            val it = iter.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.value.state == 3)
                    it.remove()
            }
        }
    }

    fun cleanRejectedApply(lhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        applyMap[lhs]?.let { iter ->
            val it = iter.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.value.state == 1)
                    it.remove()
            }
        }
    }

    fun cleanAcceptedApplied(lhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        appliedMap[lhs]?.let { iter ->
            val it = iter.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.value.state == 2)
                    it.remove()
            }
        }
    }

    fun cleanRejectedApplied(lhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        appliedMap[lhs]?.let { iter ->
            val it = iter.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.value.state == 1)
                    it.remove()
            }
        }
    }

    /**
     * @return 1已经是好友 2拉黑了对面 3被对面拉黑 0成功
     */
    fun sendFriendApply(lhs: Long, rhs: Long): Int {
        if (lhs <= AVAILABLE_PLAYER_ID || rhs <= AVAILABLE_PLAYER_ID) return -1
        if (lhs == rhs) return -1

        checkFriend(lhs, rhs).takeIf { it }?.let {
            return 1
        }

        when (checkBlackList(lhs, rhs)) {
            1 -> return 2
            2 -> return 3
        }

        val apply = ApplyInfo(lhs, rhs, System.currentTimeMillis(), 0)

        if (!applyMap.containsKey(lhs))
            applyMap[lhs] = hashMapOf()

        applyMap[lhs]!![rhs] = apply

        if (!appliedMap.containsKey(rhs))
            appliedMap[rhs] = hashMapOf()

        appliedMap[rhs]!![lhs] = apply

        return 0
    }

    fun acceptApply(lhs: Long, rhs: Long): Int {
        checkFriendApply(rhs, lhs).takeUnless{ it }?.let {
            return 1
        }

        when(addFriend(lhs, rhs)) {
            0, 1 -> { // 成功添加好友或已经是好友
                appliedMap[lhs]?.let { iter ->
                    iter[rhs]?.let {
                        it.startTime = System.currentTimeMillis()
                        it.state = 2
                    }
                }

                return 0
            }
            2 -> { // 来黑了对面默认拒绝
                rejectApply(lhs, rhs)
                return 2
            }
            3 -> { // 被对面拉黑删除这条记录
                removeApplied(lhs, rhs)
                removeApply(rhs, lhs)
                return 3
            }
        }

        return 4
    }

    fun rejectApply(lhs: Long, rhs: Long) {
        checkFriendApply(rhs, lhs).takeUnless{ it }?.let {
            return
        }

        applyMap[rhs]?.let { iter ->
            iter[lhs]?.let {
                it.startTime = System.currentTimeMillis()
                it.state = 1
            }
        }
    }

    /**
     * @return 0不在黑名单 1在自己黑名单 2在对面黑名单
     */
    fun checkBlackList(lhs: Long, rhs: Long): Int {
        if (lhs <= AVAILABLE_PLAYER_ID || rhs <= AVAILABLE_PLAYER_ID) return 0
        if (lhs == rhs) return 0

        blackListMap[lhs]?.let { iter ->
            iter[rhs]?.let {
                if (it.startTime > 0)
                    return 1
            }
        }

        blackListMap[rhs]?.let { iter ->
            iter[lhs]?.let {
                if (it.startTime > 0)
                    return 2
            }
        }

        return 0
    }

    fun sendBlackList(lhs: Long, rhs: Long) {
        val plr = PlayerManager.find(lhs) ?: return

        if (rhs > AVAILABLE_PLAYER_ID) {
            val res = blackListResponse {
                sendAll = false
                blackListMap[lhs]?.let { iter ->
                    iter[rhs]?.let {
                        list += blackListInfo {
                            pid = it.toPlayer
                            timestamp = it.startTime
                        }
                    }
                }
            }
            plr.send(ProtocolType.BLACK_LIST_RESPONSE, res.toByteArray())
            return
        }

        val res = blackListResponse {
            sendAll = true
            blackListMap[lhs]?.let { iter ->
                iter.forEach {
                    list += blackListInfo {
                        pid = it.key
                        timestamp = it.value.startTime
                    }
                }
            }
        }
        plr.send(ProtocolType.BLACK_LIST_RESPONSE, res.toByteArray())
    }

    fun addToBlackList(lhs: Long, rhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID || rhs <= AVAILABLE_PLAYER_ID) return
        if (lhs == rhs) return

        removeFriend(lhs, rhs)

        if (!blackListMap.containsKey(lhs)) {
            blackListMap[lhs] = hashMapOf()
        }

        blackListMap[lhs]!![rhs] = BlackInfo(lhs, rhs, System.currentTimeMillis())
        sendBlackList(lhs, rhs)
    }

    fun removeFromBlackList(lhs: Long, rhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID || rhs <= AVAILABLE_PLAYER_ID) return
        if (lhs == rhs) return

        blackListMap[lhs]?.remove(rhs)
        sendBlackList(lhs, rhs)
    }

    fun cleanBlackList(lhs: Long) {
        if (lhs <= AVAILABLE_PLAYER_ID) return
        blackListMap.remove(lhs)
        sendBlackList(lhs, 0)
    }
}