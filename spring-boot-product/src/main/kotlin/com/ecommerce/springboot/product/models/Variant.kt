package com.ecommerce.springboot.product.models

import java.math.BigDecimal
import java.util.*

data class CreateVariantRequest(
    val sku: String?,
    val price: BigDecimal,
    val stock: Int,
    val mediaUrl: String?,
    val weight: BigDecimal? = null,
    val dimensions: String? = null,
    val options: List<UUID>
)
