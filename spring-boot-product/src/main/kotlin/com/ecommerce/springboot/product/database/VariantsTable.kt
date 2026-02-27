package com.ecommerce.springboot.product.database

import com.ecommerce.springboot.product.database.utilities.PGEnum
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone
import java.math.BigDecimal

object VariantsTable : UUIDTable("variants") {
    enum class VariantStatus {
        ACTIVE,
        HIDDEN,
        OUT_OF_STOCK
    }

    val product = reference("product_id", ProductsTable, onDelete = ReferenceOption.CASCADE)
    val sku = varchar("sku", 50).nullable()
    val price = decimal("price", 12, 2).check { it greaterEq BigDecimal.ZERO }
    val salePrice = decimal("sale_price", 12, 2).nullable().check { it greaterEq BigDecimal.ZERO or (it.isNull()) }
    val stock = integer("stock").default(0).check { it greaterEq 0 }
    val status = customEnumeration(
        name = "status",
        sql = "variant_status",
        fromDb = { value -> VariantStatus.valueOf(value as String) },
        toDb = { PGEnum("variant_status", it) }
    ).default(VariantStatus.ACTIVE)
    val mediaUrl = varchar("media_url", 255).nullable()
    val weight = decimal("weight", 8, 2).nullable()
    val dimensions = varchar("dimensions", 50).nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}
