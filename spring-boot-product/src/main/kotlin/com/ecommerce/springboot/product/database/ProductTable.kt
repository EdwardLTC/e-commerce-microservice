package com.ecommerce.springboot.product.database

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
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
    val stockStatus = enumerationByName<StockStatus>("stock_status", 20)
    val categoryIds = array<UUID>("category_ids")
    val mediaUrls = array<String>("media_urls")
    val status = enumerationByName<ProductStatus>("status", 20)
    val totalSales = integer("total_sales")
    val rating = decimal("rating", 3, 2).default(BigDecimal(0.0))
    val version = integer("version").default(0)

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val deletedAt = timestamp("deleted_at").nullable()
}