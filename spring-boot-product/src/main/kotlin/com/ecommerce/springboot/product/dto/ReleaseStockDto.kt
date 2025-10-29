package com.ecommerce.springboot.product.dto

import jakarta.validation.constraints.NotNull
import java.util.*

class ReleaseStockDto(
    @field:NotNull val reservationId: UUID
)

