package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.CreateProductRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.*

data class CreateProductRequestDto(
    @field:NotBlank
    @field:NotNull
    @field:Size(min = 3, max = 255)
    val name: String,

    @field:NotNull
    val sellerId: UUID,

    @field:Size(max = 255)
    val description: String?,

    @field:Size(max = 255)
    val brand: String?,

    val mediaUrls: List<String> = emptyList(),
) {
    companion object : FromGrpcRequest<CreateProductRequest, CreateProductRequestDto> {
        override fun from(request: CreateProductRequest): CreateProductRequestDto {
            return CreateProductRequestDto(
                name = request.name.value,
                sellerId = UUID.fromString(request.sellerId.value),
                description = request.description.value.takeIf { it.isNotBlank() },
                brand = request.brand.value.takeIf { it.isNotBlank() },
                mediaUrls = request.mediaUrlsList.filter { it.isNotBlank() }.map { it.trim() }
            )
        }
    }
}