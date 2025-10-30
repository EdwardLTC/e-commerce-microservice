package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass
import java.util.*

data class GetVariantByIdsDto(
    val ids: List<UUID>
) {
    companion object : FromGrpcRequest<ProductOuterClass.GetVariantByIdsRequest, GetVariantByIdsDto> {
        override fun from(request: ProductOuterClass.GetVariantByIdsRequest): GetVariantByIdsDto {
            return GetVariantByIdsDto(
                ids = request.idsList.map {
                    UUID.fromString(it) ?: throw IllegalArgumentException("Variant ID must not be null")
                }
            )
        }
    }
}
