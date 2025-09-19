package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass
import jakarta.validation.constraints.NotNull
import java.util.*

data class GetOptionValuesDto(
    @field:NotNull
    val optionTypeId: UUID
) {
    companion object : FromGrpcRequest<ProductOuterClass.GetOptionValuesRequest, GetOptionValuesDto> {
        override fun from(request: ProductOuterClass.GetOptionValuesRequest): GetOptionValuesDto {
            return GetOptionValuesDto(
                optionTypeId = UUID.fromString(
                    request.optionTypeId.value ?: throw IllegalArgumentException("Option Type ID must not be null")
                )
            )
        }
    }
}