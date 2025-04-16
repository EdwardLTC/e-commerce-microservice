package com.ecommerce.springboot.product.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.math.BigDecimal
import java.util.UUID

enum class ProductStatus { DRAFT, ACTIVE, DISCONTINUED, ARCHIVED }
enum class StockStatus { IN_STOCK, OUT_OF_STOCK, BACKORDER, LIMITED }

object ProductTable : UUIDTable("products") {
    val sellerId = uuid("seller_id")
    val name = varchar("name", 255)
    val description = text("description")
    val price = decimal("price", 12, 2)
    val quantity = integer("quantity")
    val categoryIds = array<UUID>("category_ids")
    val mediaUrls = array<String>("media_urls")
    val totalSales = integer("total_sales")
    val rating = decimal("rating", 3, 2).default(BigDecimal(0.0))
    val version = integer("version").default(0)

    val status = customEnumeration(
        name = "status",
        fromDb = { value -> ProductStatus.valueOf(value as String) },
        toDb = { PGEnum("product_status", it) },
        sql = "product_status"
    )

    val stockStatus = customEnumeration(
        name = "stock_status",
        fromDb = { value -> StockStatus.valueOf(value as String) },
        toDb = { PGEnum("stock_status", it) },
        sql = "stock_status"
    )

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
    val deletedAt = datetime("deleted_at").nullable()
}