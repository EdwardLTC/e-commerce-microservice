package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.ReleaseStockDto
import com.ecommerce.springboot.product.dto.ReverseStockDto
import com.ecommerce.springboot.product.helpers.safeValidatedCall
import com.ecommerce.springboot.product.repositories.StockRepository
import com.ecommerce.springboot.product.v1.ProductOuterClass.*
import com.ecommerce.springboot.product.v1.StockServiceGrpcKt.StockServiceCoroutineImplBase
import com.google.protobuf.DoubleValue
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class StockService(private val stockRepository: StockRepository) : StockServiceCoroutineImplBase() {

    override suspend fun reserveStock(request: ReserveStockRequest): ReserveStockResponse =
        safeValidatedCall(request, ReverseStockDto) { dto ->
            val result = stockRepository.reserveStock(dto)
            return@safeValidatedCall ReserveStockResponse.newBuilder().setReservationId(result.reversalId.toString())
                .addAllVariants(result.items.map { variant ->
                    ReserveStockResponse.VariantWithProduct.newBuilder()
                        .setId(variant.id.toString())
                        .setStock(variant.stock)
                        .setPrice(variant.price)
                        .setSalePrice(variant.salePrice)
                        .setMediaUrl(variant.mediaUrl ?: "")
                        .setProductId(variant.productId.toString())
                        .setProductName(variant.productName)
                        .build()
                })
                .build()

        }

    override suspend fun releaseStock(request: ReleaseStockRequest): DoubleValue =
        safeValidatedCall(request, ReleaseStockDto) { dto ->
            return@safeValidatedCall DoubleValue.of(stockRepository.releaseStock(dto.reservationId).toDouble())
        }
}