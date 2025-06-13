package com.ecommerce.springboot.product.dto

data class CreateOptionTypeRequestDto(
    val name: String,
    val displayOrder: Int,
    val optionValues: List<CreateOptionValue> = emptyList()
)

data class CreateOptionValue(
    val value: String,
    val mediaUrl: String? = null,
    val displayOrder: Short = 1,
)