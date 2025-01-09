package org.example.manager.friend

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

//        applyMap[rhs]?.let { iter ->
//            iter[lhs]?.let {
//                return it.startTime > 0
//            }
//        }

        return false
    }

    fun sendFriendApply(lhs: Long, rhs: Long): Boolean {
        checkFriend(lhs, rhs).takeIf { it }?.let {
            return false
        }

        if (lhs <= 1000 || rhs <= 1000) return false
        if (lhs == rhs) return false

        if (!applyMap.containsKey(lhs))
            applyMap[lhs] = hashMapOf()

        applyMap[lhs]!![rhs] = ApplyInfo(lhs, rhs, System.currentTimeMillis(), 0)
        return true
    }

    fun removeApply(lhs: Long, rhs: Long) {
        if (lhs <= 1000) return
        if (lhs == rhs) return

        applyMap[lhs]?.remove(rhs)
    }

    fun cleanApply(lhs: Long) {
        if (lhs <= 1000) return

        applyMap.remove(lhs)
    }

    fun cleanAcceptedApply(lhs: Long) {
        if (lhs <= 1000) return
        applyMap[lhs]?.let { iter ->
            var it = iter.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.value.state > 1)
                    it.remove()
            }
        }
    }

    fun cleanRejectedApply(lhs: Long) {
        if (lhs <= 1000) return
        applyMap[lhs]?.let { iter ->
            var it = iter.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry.value.state == 1)
                    it.remove()
            }
        }
    }

    fun acceptApply(lhs: Long, rhs: Long): Boolean {
        checkFriendApply(lhs, rhs).takeUnless{ it }?.let {
            return false
        }

        addFriend(lhs, rhs).takeIf { it == 0 }?.let {

            applyMap[lhs]?.let { iter ->
                iter[rhs]?.let { it ->
                    it.startTime = System.currentTimeMillis()
                    it.state = 2
                }
            }

            applyMap[rhs]?.let { iter ->
                iter[lhs]?.let { it ->
                    it.startTime = System.currentTimeMillis()
                    it.state = 3
                }
            }

            return true
        }

        return false
    }

    fun rejectApply(lhs: Long, rhs: Long) {
        checkFriendApply(lhs, rhs).takeUnless{ it }?.let {
            return
        }

        applyMap[lhs]?.let { iter ->
            iter[rhs]?.let { it ->
                it.startTime = System.currentTimeMillis()
                it.state = 1
            }
        }
    }

    fun checkBlackList(lhs: Long, rhs: Long): Boolean {
        if (lhs <= 1000 || rhs <= 1000) return false
        if (lhs == rhs) return false

        blackListMap[lhs]?.let { iter ->
            iter[rhs]?.let { it ->
                return it.startTime > 0
            }
        }

        blackListMap[rhs]?.let { iter ->
            iter[lhs]?.let { it ->
                return it.startTime > 0
            }
        }

        return false
    }

    fun addToBlackList(lhs: Long, rhs: Long) {
        if (lhs <= 1000 || rhs <= 1000) return
        if (lhs == rhs) return

        removeFriend(lhs, rhs)

        if (!blackListMap.containsKey(lhs)) {
            blackListMap[lhs] = hashMapOf()
        }

        blackListMap[lhs]!![rhs] = BlackInfo(lhs, rhs, System.currentTimeMillis())
    }

    fun removeFromBlackList(lhs: Long, rhs: Long) {
        if (lhs <= 1000 || rhs <= 1000) return
        if (lhs == rhs) return

        blackListMap[lhs]?.remove(rhs)
    }

    fun cleanBlackList(lhs: Long) {
        if (lhs <= 1000) return
        blackListMap.remove(lhs)
    }
}