package org.example.base.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.example.base.table.AvatarTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DataAccess {

    private val logger: Logger = LogManager.getLogger(this::class.java)

    init {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:mysql://localhost:3306/testdb"
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = "root"
            password = "1234"
            maximumPoolSize = 10  // 允许最多 10 个并发连接
        }
        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)
    }

    fun createTable() {
        logger.info("Connected to database")

        transaction {
             SchemaUtils.create(AvatarTable)
        }
    }
}