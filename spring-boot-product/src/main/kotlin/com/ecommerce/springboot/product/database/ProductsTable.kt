package com.ecommerce.springboot.product.database

import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object ProductsTable : UUIDTable("products") {
    val sellerId = varchar("seller_id", 36)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val brand = varchar("brand", 100).nullable()
    val mediaUrls = array<String>("media_urls").default(emptyList())
    val isActive = bool("is_active").default(true)
    val totalSaleCount = integer("total_sale_count").default(0)
    val averageRating = double("average_rating").default(0.0)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}