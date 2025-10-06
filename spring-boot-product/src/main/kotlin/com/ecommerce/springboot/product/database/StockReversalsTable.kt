package com.ecommerce.springboot.product.database

import com.ecommerce.springboot.product.database.utilities.PGEnum
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object StockReversalsTable : UUIDTable("stock_reversals") {

    enum class StockCheckinStatus {
        PENDING,
        RELEASED,
        REVERSED,
    }

    val status = customEnumeration(
        name = "status",
        sql = "stock_reversal_status",
        fromDb = { value -> StockCheckinStatus.valueOf(value as String) },
        toDb = { PGEnum("stock_reversal_status", it) }
    ).default(StockCheckinStatus.PENDING)

    val checkinAt = timestampWithTimeZone("checkin_at")
    val releaseAt = timestampWithTimeZone("release_at").nullable().default(null)
}