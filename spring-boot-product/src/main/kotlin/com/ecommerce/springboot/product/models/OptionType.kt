package com.ecommerce.springboot.product.models

import java.util.*

data class CreateOptionTypeRequest(
    val name: String,
    val displayOrder: Int,
    val optionValues: List<CreateOptionValue> = emptyList()
)

data class CreateOptionValue(
    val value: String,
    val mediaUrl: String? = null,
    val displayOrder: Short = 1,
)

data class OptionType(
    val id: String,
    val productId: String,
    val name: String,
    val displayOrder: Short,
    var optionValues: List<OptionValue> = emptyList()
)

data class OptionValue(
    val id: String,
    val value: String,
    val mediaUrl: String? = null,
    val displayOrder: Short = 1
)

data class OptionValueWithProductId(
    val id: String,
    val value: String,
    val mediaUrl: String? = null,
    val displayOrder: Short = 1,
    val productId: UUID
)