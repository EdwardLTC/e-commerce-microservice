package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.CreateVariantDto
import com.ecommerce.springboot.product.dto.GetVariantByIdsDto
import com.ecommerce.springboot.product.helpers.safeValidatedCall
import com.ecommerce.springboot.product.repositories.VariantRepository
import com.ecommerce.springboot.product.v1.ProductOuterClass
import com.ecommerce.springboot.product.v1.ProductOuterClass.CreateVariantRequest
import com.ecommerce.springboot.product.v1.ProductOuterClass.CreateVariantResponse
import com.ecommerce.springboot.product.v1.VariantServiceGrpcKt.VariantServiceCoroutineImplBase
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class VariantService(private val variantRepository: VariantRepository) : VariantServiceCoroutineImplBase() {
    override suspend fun createVariant(request: CreateVariantRequest): CreateVariantResponse =
        safeValidatedCall(request, CreateVariantDto) { dto ->
            return@safeValidatedCall CreateVariantResponse.newBuilder().setId(variantRepository.create(dto).toString())
                .build()
        }

    override suspend fun getVariantsByIds(request: ProductOuterClass.GetVariantByIdsRequest): ProductOuterClass.GetVariantsResponse =
        safeValidatedCall(request, GetVariantByIdsDto) { dto ->
            val variants = variantRepository.getByVariantIds(dto.ids)
            val responseBuilder = ProductOuterClass.GetVariantsResponse.newBuilder()
            variants.forEach {
                responseBuilder.addVariants(
                    ProductOuterClass.GetVariantsResponse.Variant.newBuilder()
                        .setId(it.id.toString())
                        .setSku(it.sku)
                        .setPrice(it.price)
                        .setSalePrice(it.salePrice)
                        .setStock(it.stock)
                        .setMediaUrl(it.mediaUrl)
                        .setProductId(it.productId.toString())
                        .setProductName(it.productName)
                        .build()
                )
            }
            return@safeValidatedCall responseBuilder.build()
        }

}