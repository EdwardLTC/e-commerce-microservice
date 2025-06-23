package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.GetOptionTypesRequest
import jakarta.validation.constraints.NotNull
import java.util.*

data class GetOptionTypesDto(
    @field:NotNull
    val productId: UUID,
) {
    companion object : FromGrpcRequest<GetOptionTypesRequest, GetProductDetailDto> {
        override fun from(request: GetOptionTypesRequest): GetProductDetailDto {
            return GetProductDetailDto(
                productId = UUID.fromString(
                    request.productId.value ?: throw IllegalArgumentException("Product ID must not be null")
                )
            )
        }
    }
}