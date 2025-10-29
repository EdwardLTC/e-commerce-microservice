package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.CreateVariantDto
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

    override suspend fun getVariantsByIds(request: ProductOuterClass.GetVariantByIdsRequest): ProductOuterClass.GetVariantsResponse {
        return super.getVariantsByIds(request)
    }
}