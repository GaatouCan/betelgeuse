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

    private suspend fun <Temp> dbQuery(block: suspend () -> Temp): Temp =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun insert(block: T.(InsertStatement<Number>) -> Unit): Unit = dbQuery {
        table.insert { block(it) }
    }

    suspend fun upsert(body: T.(UpsertStatement<Long>) -> Unit): Unit = dbQuery {
        table.upsert { body(it) }
    }

    suspend fun update(where: SqlExpressionBuilder.() -> Op<Boolean>, block: T.(UpdateStatement) -> Unit): Int = dbQuery {
        table.update({ where() }) { block(it) }
    }

    suspend fun delete(where: T.(ISqlExpressionBuilder) -> Op<Boolean>): Int = dbQuery {
        table.deleteWhere { where(it) }
    }

    suspend fun replace(block: T.(UpdateBuilder<*>) -> Unit): Unit = dbQuery {
        table.replace { block(it) }
    }

    suspend fun find(where: SqlExpressionBuilder.() -> Op<Boolean>): ResultRow? = dbQuery {
        table.selectAll().where { this.where() }.singleOrNull()
    }

    suspend fun findAll(where: SqlExpressionBuilder.() -> Op<Boolean>): List<ResultRow> = dbQuery {
        table.selectAll().where { this.where() }.toList()
    }
}