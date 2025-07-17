package com.ecommerce.springboot.product.dto

import com.ecommerce.springboot.product.helpers.FromGrpcRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.ReleaseStockRequest
import jakarta.validation.constraints.NotNull
import java.util.*

class ReleaseStockDto(
    @field:NotNull val reservationId: UUID
) {
    companion object : FromGrpcRequest<ReleaseStockRequest, ReleaseStockDto> {
        override fun from(request: ReleaseStockRequest): ReleaseStockDto {
            return ReleaseStockDto(
                reservationId = UUID.fromString(request.reservationId)
            )
        }
    }
}

