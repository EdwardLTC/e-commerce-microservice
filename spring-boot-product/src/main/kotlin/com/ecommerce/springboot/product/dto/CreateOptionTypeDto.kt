package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.CreateOptionTypeRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.CreateOptionValueRequest
import org.jetbrains.annotations.NotNull
import org.springframework.format.annotation.NumberFormat
import java.util.*

data class CreateOptionTypeDto(
    @field:NotNull
    val productId: UUID,

    @field:NotNull
    val name: String,

    @field:NumberFormat
    val displayOrder: Int = 1,
) {
    companion object : FromGrpcRequest<CreateOptionTypeRequest, CreateOptionTypeDto> {
        override fun from(request: CreateOptionTypeRequest): CreateOptionTypeDto {
            return CreateOptionTypeDto(
                productId = UUID.fromString(
                    request.productId.value ?: throw IllegalArgumentException("Product ID must not be null")
                ),
                name = request.name.value,
                displayOrder = request.displayOrder.value
            )
        }
    }
}

data class CreateOptionValueDto(
    @field:NotNull
    val optionTypeId: UUID,

    @field:NotNull
    val value: String,

    val mediaUrl: String? = null,

    @field:NumberFormat
    val displayOrder: Short = 1,
) {
    companion object : FromGrpcRequest<CreateOptionValueRequest, CreateOptionValueDto> {
        override fun from(request: CreateOptionValueRequest): CreateOptionValueDto {
            return CreateOptionValueDto(
                optionTypeId = UUID.fromString(
                    request.optionTypeId.value ?: throw IllegalArgumentException("Option Type ID must not be null")
                ),
                value = request.value.value,
                mediaUrl = request.mediaUrl.value.takeIf { it.isNotBlank() },
                displayOrder = request.displayOrder.value.toShort()
            )
        }
    }
}