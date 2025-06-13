package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.CreateProductRequestDto
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
            return@safeValidatedCall CreateProductResponse.newBuilder().setId(productRepository.create(dto).toString())
                .build()
        }

}
