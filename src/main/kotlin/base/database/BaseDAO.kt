package org.example.base.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.statements.UpsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


abstract class BaseDAO<T : Table> {
    abstract val table: T

    // 内部封装协程事务
    private suspend fun <Temp> transactionInner(block: suspend () -> Temp): Temp =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun insert(block: T.(InsertStatement<Number>) -> Unit): Unit = transactionInner {
        table.insert { block(it) }
    }

    suspend fun upsert(body: T.(UpsertStatement<Long>) -> Unit): Unit = transactionInner {
        table.upsert { body(it) }
    }

    suspend fun update(where: SqlExpressionBuilder.() -> Op<Boolean>, block: T.(UpdateStatement) -> Unit): Int = transactionInner {
        table.update({ where() }) { block(it) }
    }

    suspend fun delete(where: T.(ISqlExpressionBuilder) -> Op<Boolean>): Int = transactionInner {
        table.deleteWhere { where(it) }
    }

    suspend fun replace(block: T.(UpdateBuilder<*>) -> Unit): Unit = transactionInner {
        table.replace { block(it) }
    }

    suspend fun find(where: SqlExpressionBuilder.() -> Op<Boolean>): ResultRow? = transactionInner {
        table.selectAll().where { this.where() }.singleOrNull()
    }

    suspend fun findAll(where: SqlExpressionBuilder.() -> Op<Boolean>): List<ResultRow> = transactionInner {
        table.selectAll().where { this.where() }.toList()
    }
}