package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.CreateVariantDto
import com.ecommerce.springboot.product.dto.ReleaseStockDto
import com.ecommerce.springboot.product.dto.ReverseStockDto
import com.ecommerce.springboot.product.helpers.safeValidatedCall
import com.ecommerce.springboot.product.repositories.VariantRepository
import com.ecommerce.springboot.product.v1.ProductOuterClass.*
import com.ecommerce.springboot.product.v1.VariantServiceGrpcKt.VariantServiceCoroutineImplBase
import com.google.protobuf.DoubleValue
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class VariantService(private val variantRepository: VariantRepository) : VariantServiceCoroutineImplBase() {
    override suspend fun createVariant(request: CreateVariantRequest): CreateVariantResponse =
        safeValidatedCall(request, CreateVariantDto) { dto ->
            return@safeValidatedCall CreateVariantResponse.newBuilder().setId(variantRepository.create(dto).toString())
                .build()
        }

    override suspend fun reserveStock(request: ReserveStockRequest): ReserveStockResponse =
        safeValidatedCall(request, ReverseStockDto) { dto ->
            val result = variantRepository.reserveStock(dto)
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
            return@safeValidatedCall DoubleValue.of(variantRepository.releaseStock(dto.reservationId).toDouble())
        }
}