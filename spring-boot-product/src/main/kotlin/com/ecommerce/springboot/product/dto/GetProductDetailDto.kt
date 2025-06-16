package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.GetProductDetailRequest
import jakarta.validation.constraints.NotNull
import java.util.*

data class GetProductDetailDto(
    @field:NotNull
    val productId: UUID,
) {
    companion object : FromGrpcRequest<GetProductDetailRequest, GetProductDetailDto> {
        override fun from(request: GetProductDetailRequest): GetProductDetailDto {
            return GetProductDetailDto(
                productId = UUID.fromString(
                    request.id.value ?: throw IllegalArgumentException("Product ID must not be null")
                )
            )
        }
    }
}