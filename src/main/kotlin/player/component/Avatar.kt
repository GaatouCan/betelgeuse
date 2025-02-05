package org.example.player.component

data class Avatar(
    val pid : Long,
    val index : Int,
    var activated : Boolean,
    var inUsed: Boolean
)
