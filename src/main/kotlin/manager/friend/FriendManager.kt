package org.example.manager.friend

object FriendManager {

    // 玩家好友信息二维哈希表
    private val friendMap = hashMapOf<Long, HashMap<Long, FriendInfo>>()

    init {

    }

    fun checkFriend(lhs: Long, rhs: Long): Boolean {
        if (lhs <= 1000 || rhs <= 1000) return false
        if (lhs == rhs) return false

        friendMap[lhs]?.let{
            it[rhs]?.let {
                return it.startTime > 0
            }
        }

        friendMap[rhs]?.let{
            it[lhs]?.let {
                return it.startTime > 0
            }
        }

        return false
    }

    fun addFriend(lhs: Long, rhs: Long) : Int {
        checkFriend(lhs, rhs).takeIf { it }?.let {
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
}