package org.example

import proto.player.clientLoginRequest

fun main() {
    val name = "Kotlin"
    println("Hello, $name!")

    for (i in 1..5) {
        println("i = $i")
    }

    val req = clientLoginRequest {
        id = 100001
        token = "Hello, World!"
    }
    println(req.toByteArray())
}