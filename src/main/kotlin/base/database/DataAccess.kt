package org.example.base.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DataAccess {

    private fun connectWithHikari() {
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
}