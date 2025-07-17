package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.ReserveStockRequest
import java.util.*

data class ReserveStock(
    val variantId: UUID,
    val quantity: Int,
)

class ReverseStockDto(items: List<ReserveStock> = emptyList()) : List<ReserveStock> by items {
    companion object : FromGrpcRequest<ReserveStockRequest, ReverseStockDto> {
        override fun from(request: ReserveStockRequest): ReverseStockDto {
            return request.itemsList.map {
                ReserveStock(
                    variantId = UUID.fromString(
                        it.variantId ?: throw IllegalArgumentException("Variant ID must not be null")
                    ),
                    quantity = it.quantity.takeIf { it > 0 }
                        ?: throw IllegalArgumentException("Quantity must be greater than zero")
                )
            }.let { ReverseStockDto(it) }
        }
    }
}

