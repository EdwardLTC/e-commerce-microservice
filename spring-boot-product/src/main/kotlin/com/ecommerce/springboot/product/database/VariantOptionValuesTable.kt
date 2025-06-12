package com.ecommerce.springboot.product.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable

object VariantOptionValuesTable : UUIDTable("variant_option_values") {
    val variant = reference("variant_id", VariantsTable, onDelete = ReferenceOption.CASCADE)
    val optionValue = reference("option_value_id", OptionValuesTable, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex("variant_option_unique", variant, optionValue)
    }
}