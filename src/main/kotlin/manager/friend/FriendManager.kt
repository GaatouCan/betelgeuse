package org.example.manager.friend

object FriendManager {

    // 玩家好友信息二维哈希表
    private val friendMap = hashMapOf<Long, HashMap<Long, FriendInfo>>()

    init {

    }

    fun checkFriend(lhs: Long, rhs: Long): Boolean {
        if (lhs <= 1000 || rhs <= 1000) return false
        if (lhs == rhs) return false
        if (friendMap.containsKey(lhs)) {
            val iter = friendMap[lhs]!!
            if (iter.contains(rhs)) {
                if (iter[rhs]!!.startTime > 0)
                    return true
            }
        }
        if (friendMap.containsKey(rhs)) {
            val iter = friendMap[rhs]!!
            if (iter.contains(lhs)) {
                if (iter[lhs]!!.startTime > 0)
                    return true
            }
        }
        return false
    }
}