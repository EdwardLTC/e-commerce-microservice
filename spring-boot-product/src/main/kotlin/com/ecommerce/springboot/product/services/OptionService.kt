package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.CreateOptionTypeDto
import com.ecommerce.springboot.product.dto.CreateOptionValueDto
import com.ecommerce.springboot.product.dto.GetOptionTypesDto
import com.ecommerce.springboot.product.dto.GetOptionValuesDto
import com.ecommerce.springboot.product.helpers.safeValidatedCall
import com.ecommerce.springboot.product.repositories.OptionRepository
import com.ecommerce.springboot.product.v1.OptionServiceGrpcKt.OptionServiceCoroutineImplBase
import com.ecommerce.springboot.product.v1.ProductOuterClass.*
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class OptionService(private val optionTypeRepository: OptionRepository) : OptionServiceCoroutineImplBase() {
    override suspend fun createOptionType(request: CreateOptionTypeRequest): CreateOptionTypeResponse =
        safeValidatedCall(request, CreateOptionTypeDto) { dto ->
            return@safeValidatedCall CreateOptionTypeResponse.newBuilder()
                .setId(optionTypeRepository.create(dto).id)
                .build()
        }

    override suspend fun createOptionValue(request: CreateOptionValueRequest): CreateOptionValueResponse =
        safeValidatedCall(request, CreateOptionValueDto) { dto ->
            return@safeValidatedCall CreateOptionValueResponse.newBuilder()
                .setId(optionTypeRepository.createOptionValue(dto).toString())
                .build()
        }

    override suspend fun getOptionTypes(request: GetOptionTypesRequest): GetOptionTypesResponse =
        safeValidatedCall(request, GetOptionTypesDto) { dto ->
            val optionTypes = optionTypeRepository.getByProductId(dto.productId)
            return@safeValidatedCall GetOptionTypesResponse.newBuilder()
                .addAllOptionTypes(
                    optionTypes.map { optionType ->
                        OptionType.newBuilder()
                            .setId(optionType.id)
                            .setName(optionType.name)
                            .setDisplayOrder(optionType.displayOrder.toInt())
                            .addAllOptionValues(
                                optionType.optionValues.map { optionValue ->
                                    OptionValue.newBuilder()
                                        .setId(optionValue.id)
                                        .setValue(optionValue.value)
                                        .setDisplayOrder(optionValue.displayOrder.toInt())
                                        .setMediaUrl(optionValue.mediaUrl ?: "")
                                        .build()
                                }
                            )
                            .build()
                    }
                )
                .build()

        }

    override suspend fun getOptionValues(request: GetOptionValuesRequest): GetOptionValuesResponse =
        safeValidatedCall(request, GetOptionValuesDto) { dto ->
            val optionValue = optionTypeRepository.getOptionValuesByOptionTypeId(dto.optionTypeId)
            return@safeValidatedCall GetOptionValuesResponse.newBuilder().addAllOptionValues(
                optionValue.map { optionValue ->
                    OptionValue.newBuilder()
                        .setId(optionValue.id)
                        .setValue(optionValue.value)
                        .setDisplayOrder(optionValue.displayOrder.toInt())
                        .setMediaUrl(optionValue.mediaUrl ?: "")
                        .build()
                }
            ).build()
        }
    
}