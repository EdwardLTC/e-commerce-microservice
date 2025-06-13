package com.ecommerce.springboot.product.dto

import java.math.BigDecimal
import java.util.*

data class CreateVariantRequestDto(
    val sku: String?,
    val price: BigDecimal,
    val stock: Int,
    val mediaUrl: String?,
    val weight: BigDecimal? = null,
    val dimensions: String? = null,
    val options: List<UUID>
) {
//    companion object : FromGrpcRequest<CreateVariantRequestDto, CreateVariantRequestDto> {
//        override fun from(request: CreateVariantRequestDto): CreateVariantRequestDto {
//            return CreateVariantRequestDto(
//                sku = request.sku,
//                price = request.price,
//                stock = request.stock,
//                mediaUrl = request.mediaUrl,
//                weight = request.weight,
//                dimensions = request.dimensions,
//                options = request.options
//            )
//        }
//    }
}
