package com.ecommerce.springboot.product.dto

import java.util.*

data class ReserveStock(
    val variantId: UUID,
    val quantity: Int,
    val unitPrice: Double,
)


