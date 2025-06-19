package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.CreateProductRequestDto
import com.ecommerce.springboot.product.dto.GetProductDetailDto
import com.ecommerce.springboot.product.dto.GetProductsRequestDto
import com.ecommerce.springboot.product.helpers.safeValidatedCall
import com.ecommerce.springboot.product.repositories.ProductRepository
import com.ecommerce.springboot.product.v1.ProductOuterClass.*
import com.ecommerce.springboot.product.v1.ProductServiceGrpcKt.ProductServiceCoroutineImplBase
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService()
class ProductService(private val productRepository: ProductRepository) : ProductServiceCoroutineImplBase() {
    override suspend fun getProducts(request: GetProductsRequest): GetProductsResponse =
        safeValidatedCall(request, GetProductsRequestDto) { dto ->
            return@safeValidatedCall GetProductsResponse.newBuilder()
                .addAllProducts(
                    productRepository.getProducts(dto.skip, dto.take).map { product ->
                        Product.newBuilder()
                            .setId(product.id.toString())
                            .setName(product.name)
                            .setDescription(product.description ?: "")
                            .setBrand(product.brand ?: "")
                            .setMinPrice(product.minPrice.toDouble())
                            .setMaxPrice(product.maxPrice.toDouble())
                            .setRating(product.rating)
                            .addAllMediaUrls(product.mediaUrls)
                            .setTotalSaleCount(product.totalSaleCount)
                            .build()
                    }
                )
                .build()

        }

    override suspend fun createProduct(request: CreateProductRequest): CreateProductResponse =
        safeValidatedCall(request, CreateProductRequestDto) { dto ->
            return@safeValidatedCall CreateProductResponse.newBuilder()
                .setId(productRepository.create(dto).toString())
                .build()
        }

    override suspend fun getProductDetail(request: GetProductDetailRequest): GetProductDetailResponse =
        safeValidatedCall(request, GetProductDetailDto) { dto ->
            val product = productRepository.getProductDetail(dto.productId)

            return@safeValidatedCall GetProductDetailResponse.newBuilder()
                .setId(product.id.toString())
                .setName(product.name)
                .setDescription(product.description ?: "")
                .setBrand(product.brand ?: "")
                .addAllMediaUrls(product.mediaUrls)
                .setTotalSaleCount(product.totalSaleCount)
                .setAverageRating(product.averageRating)
                .addAllOptionTypes(
                    product.optionTypes.map { optionType ->
                        OptionType.newBuilder()
                            .setId(optionType.id.toString())
                            .setName(optionType.name)
                            .setDisplayOrder(optionType.displayOrder.toInt())
                            .addAllOptionValues(
                                optionType.optionValues.map { optionValue ->
                                    OptionValue.newBuilder()
                                        .setId(optionValue.id.toString())
                                        .setValue(optionValue.value)
                                        .setDisplayOrder(optionValue.displayOrder.toInt())
                                        .setMediaUrl(optionValue.mediaUrl ?: "")
                                        .build()
                                }
                            )
                            .build()
                    }
                )
                .addAllVariants(
                    product.variants.map { variant ->
                        Variant.newBuilder()
                            .setId(variant.id.toString())
                            .setSku(variant.sku ?: "")
                            .setPrice(variant.price)
                            .setSalePrice(variant.salePrice ?: 0.0)
                            .setStock(variant.stock)
                            .setStatus(variant.status)
                            .setMediaUrl(variant.mediaUrl ?: "")
                            .addAllSelectedOptions(
                                variant.selectedOptions.map { selectedOption ->
                                    SelectedOption.newBuilder()
                                        .setOptionTypeId(selectedOption.optionTypeId.toString())
                                        .setOptionValueId(selectedOption.optionValueId.toString())
                                        .build()
                                }
                            )
                            .build()
                    }
                )
                .build()
        }
}
