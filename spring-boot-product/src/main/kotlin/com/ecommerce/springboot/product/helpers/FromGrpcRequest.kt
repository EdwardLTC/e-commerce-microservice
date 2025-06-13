package com.ecommerce.springboot.product.helpers

interface FromGrpcRequest<in Req, out DTO> {
    fun from(request: Req): DTO
}