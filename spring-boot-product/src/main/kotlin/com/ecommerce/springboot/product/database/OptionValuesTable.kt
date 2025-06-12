package com.ecommerce.springboot.product.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object OptionValuesTable : UUIDTable("option_values") {
    val optionType = reference("option_type_id", OptionTypesTable, onDelete = ReferenceOption.CASCADE)
    val value = varchar("value", 100)
    val mediaUrl = text("media_url").nullable()
    val displayOrder = short("display_order").default(1)
}