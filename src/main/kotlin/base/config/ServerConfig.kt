package org.example.base.config

data class ServerConfig(
    val server: Server,
    val database: Database,
) {
    data class Server(
        val host: String,
        val port: Int,
        val worker: Int,
    )
    data class Database(
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val schema: String,
    )
}