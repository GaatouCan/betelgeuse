package org.example.manager.friend

data class ApplyInfo(
    val fromPlayer: Long,
    val toPlayer: Long,
    var startTime: Long,
    var state: Int  // 0: 等待 1: 拒绝 2: 接受
)
