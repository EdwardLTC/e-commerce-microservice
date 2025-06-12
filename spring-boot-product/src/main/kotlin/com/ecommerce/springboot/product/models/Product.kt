package com.ecommerce.springboot.product.models

import kotlinx.datetime.Instant
import java.math.BigDecimal

data class CreateProductRequest(
    val name: String,
    val description: String?,
    val brand: String?,
    val mediaUrls: List<String>,
)

data class GetProductByIdResponse(
    val id: String,
    val name: String,
    val description: String?,
    val brand: String?,
    val mediaUrls: List<String>,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class GetProductResponse(
    val id: String,
    val name: String,
    val description: String?,
    val brand: String?,
    val mediaUrls: List<String>,
    val minPrice: BigDecimal,
    val maxPrice: BigDecimal,
    val rating: Double,
    val totalSaleCount: Int,
)