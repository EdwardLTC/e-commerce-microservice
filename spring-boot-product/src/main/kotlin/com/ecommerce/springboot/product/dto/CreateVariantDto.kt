package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.CreateVariantRequest
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.NumberFormat
import java.math.BigDecimal
import java.util.*

data class CreateVariantDto(
    @field:NotNull
    val productId: UUID,
    val sku: String?,

    @field:NotNull
    @field:NumberFormat
    @field:Min(1000)
    val price: BigDecimal,

    @field:NotNull
    @field:NumberFormat
    @field:Min(1)
    val stock: Int,

    val mediaUrl: String?,
    val weight: BigDecimal? = null,
    val dimensions: String? = null,
    val options: List<UUID>
) {
    companion object : FromGrpcRequest<CreateVariantRequest, CreateVariantDto> {
        override fun from(request: CreateVariantRequest): CreateVariantDto {
            return CreateVariantDto(
                productId = UUID.fromString(
                    request.productId.value ?: throw IllegalArgumentException("Product ID must not be null")
                ),
                sku = request.sku.value.takeIf { it.isNotBlank() },
                price = request.price.value.toBigDecimal(),
                stock = request.stock.value,
                mediaUrl = request.mediaUrl.value.takeIf { it.isNotBlank() },
                options = request.optionsList.toSet().map { option -> UUID.fromString(option) },
            )
        }
    }
}
