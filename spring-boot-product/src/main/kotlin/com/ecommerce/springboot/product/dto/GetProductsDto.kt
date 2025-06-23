package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.GetProductsRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.format.annotation.NumberFormat

data class GetProductsDto(
    @field:NumberFormat(style = NumberFormat.Style.NUMBER)
    @field:Min(0)
    val skip: Int,

    @field:NumberFormat(style = NumberFormat.Style.NUMBER)
    @field:Min(1)
    @field:Max(100)
    val take: Int
) {
    companion object : FromGrpcRequest<GetProductsRequest, GetProductsDto> {
        override fun from(request: GetProductsRequest): GetProductsDto {
            return GetProductsDto(
                skip = request.skip.value,
                take = request.take.value
            )
        }
    }
}
