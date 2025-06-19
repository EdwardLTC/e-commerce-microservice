package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.CreateOptionTypeDto
import com.ecommerce.springboot.product.dto.CreateOptionValueDto
import com.ecommerce.springboot.product.helpers.safeValidatedCall
import com.ecommerce.springboot.product.repositories.OptionRepository
import com.ecommerce.springboot.product.v1.OptionServiceGrpcKt.OptionServiceCoroutineImplBase
import com.ecommerce.springboot.product.v1.ProductOuterClass.*
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService()
class OptionService(private val optionTypeRepository: OptionRepository) : OptionServiceCoroutineImplBase() {
    override suspend fun createOptionType(request: CreateOptionTypeRequest): CreateOptionTypeResponse =
        safeValidatedCall(request, CreateOptionTypeDto) { dto ->
            return@safeValidatedCall CreateOptionTypeResponse.newBuilder()
                .setId(optionTypeRepository.create(dto).id.toString())
                .build()
        }

    override suspend fun createOptionValue(request: CreateOptionValueRequest): CreateOptionValueResponse =
        safeValidatedCall(request, CreateOptionValueDto) { dto ->
            return@safeValidatedCall CreateOptionValueResponse.newBuilder()
                .setId(optionTypeRepository.createOptionValue(dto).toString())
                .build()
        }
}