package com.ecommerce.springboot.product.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object StockReversalItemsTable : UUIDTable("stock_reversal_items") {
    val reversal = reference("reversal_id", StockReversalsTable, onDelete = ReferenceOption.CASCADE)
    val variant = reference("variant_id", VariantsTable, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")

    init {
        uniqueIndex("reversal_variant_unique", reversal, variant)
    }
}