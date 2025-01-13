package org.example.controller

enum class ProtocolType(val value: Int) {
    // player
    CLIENT_LOGIN_REQUEST(1001),

    // friend
    FRIEND_LIST_REQUEST(2001),
    FRIEND_LIST_RESPONSE(2002),
}