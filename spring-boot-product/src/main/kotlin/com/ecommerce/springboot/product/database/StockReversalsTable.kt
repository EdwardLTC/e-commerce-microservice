package com.ecommerce.springboot.product.database

import com.ecommerce.springboot.product.database.utilities.PGEnum
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

object StockReversalsTable : UUIDTable("stock_reversals") {

    enum class StockCheckinStatus {
        RELEASED,
        REVERSED,
    }

    val status = customEnumeration(
        name = "status",
        sql = "stock_reversal_status",
        fromDb = { value -> StockCheckinStatus.valueOf(value as String) },
        toDb = { PGEnum("stock_reversal_status", it) }
    ).default(StockCheckinStatus.RELEASED)

    val orderId = varchar("orderId", 36).uniqueIndex()
}