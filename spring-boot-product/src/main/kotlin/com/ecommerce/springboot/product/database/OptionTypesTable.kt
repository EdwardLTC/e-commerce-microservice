package com.ecommerce.springboot.product.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable

object OptionTypesTable : UUIDTable("option_types") {
    val product = reference("product_id", ProductsTable, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
    val displayOrder = short("display_order").default(1)
}