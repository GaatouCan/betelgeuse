package org.example.manager.friend

object FriendManager {

    // 玩家好友信息二维哈希表
    private val friendMap = hashMapOf<Long, HashMap<Long, FriendInfo>>()

    init {

    }

    /**
     * @return 0: 不是好友; 1: 左边是第一层键; -1: 右边是第一层键
     */
    fun checkFriend(lhs: Long, rhs: Long): Int {
        if (lhs <= 1000 || rhs <= 1000) return 0
        if (lhs == rhs) return 0

        friendMap[lhs]?.let {
            it[rhs]?.let {
                return if (it.startTime > 0) 1
                else 0
            }
        }

        friendMap[rhs]?.let {
            it[lhs]?.let {
                return if (it.startTime > 0) -1
                else 0
            }
        }

        return 0
    }

    fun addFriend(lhs: Long, rhs: Long): Int {
        checkFriend(lhs, rhs).takeUnless { it == 0 }?.let {
            return 1
        }

        // TODO: 加好友限制检查

        if (!friendMap.containsKey(lhs)) {
            friendMap[lhs] = hashMapOf<Long, FriendInfo>()
        }

        friendMap[lhs]!![rhs] = FriendInfo(lhs, rhs, System.currentTimeMillis())

        // TODO: 同步数据库

        return 0
    }

    fun removeFriend(lhs: Long, rhs: Long): Int {
        when (checkFriend(lhs, rhs)) {
            0 -> return 0
            1 -> {
                friendMap[lhs]?.let {
                    if (it.containsKey(rhs)) {
                        it.remove(rhs)
                        return 0
                    }
                }
            }
            -1 -> {
                friendMap[rhs]?.let {
                    if (it.containsKey(lhs)) {
                        it.remove(lhs)
                        return 0
                    }
                }
            }
        }

        return 2
    }
}