﻿package org.example.manager.friend

object FriendManager {

    // 玩家好友信息二维哈希表
    private val friendMap = hashMapOf<Long, HashMap<Long, FriendInfo>>()

    private val applyMap = hashMapOf<Long, HashMap<Long, ApplyInfo>>()
    private val blackListMap = hashMapOf<Long, HashMap<Long, BlackInfo>>()

    init {

    }

    fun checkFriend(lhs: Long, rhs: Long): Boolean {
        if (lhs <= 1000 || rhs <= 1000) return false
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

    fun addFriend(lhs: Long, rhs: Long): Int {
        checkFriend(lhs, rhs).takeIf { it }?.let {
            return 1
        }

        // TODO: 加好友限制检查

        if (!friendMap.containsKey(lhs))
            friendMap[lhs] = hashMapOf()
        friendMap[lhs]!![rhs] = FriendInfo(lhs, rhs, System.currentTimeMillis())

        if (!friendMap.containsKey(rhs))
            friendMap[rhs] = hashMapOf()
        friendMap[rhs]!![lhs] = FriendInfo(rhs, lhs, System.currentTimeMillis())

        // TODO: 同步数据库

        return 0
    }

    fun removeFriend(lhs: Long, rhs: Long) {
        checkFriend(lhs, rhs).takeUnless { it }?.let {
            return
        }

        friendMap[lhs]?.remove(rhs)
        friendMap[rhs]?.remove(lhs)
    }

    fun checkFriendApply(lhs: Long, rhs: Long): Boolean {
        if (lhs <= 1000 || rhs <= 1000) return false
        if (lhs == rhs) return false

        applyMap[lhs]?.let { iter ->
            iter[rhs]?.let {
                return it.startTime > 0
            }
        }

        applyMap[rhs]?.let { iter ->
            iter[lhs]?.let {
                return it.startTime > 0
            }
        }

        return false
    }

    fun sendFriendApply(lhs: Long, rhs: Long): Boolean {
        if (lhs <= 1000 || rhs <= 1000) return false
        if (lhs == rhs) return false

        if (!applyMap.containsKey(lhs))
            applyMap[lhs] = hashMapOf()

        applyMap[lhs]!![rhs] = ApplyInfo(lhs, rhs, System.currentTimeMillis())
        return false
    }

    fun removeApply(lhs: Long, rhs: Long) {
        checkFriendApply(lhs, rhs).takeUnless { it }?.let {
            return
        }

        applyMap[lhs]?.remove(rhs)
        applyMap[rhs]?.remove(lhs)
    }
}