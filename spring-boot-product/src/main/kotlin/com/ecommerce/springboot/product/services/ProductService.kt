package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.repositories.ProductRepository
import com.ecommerce.springboot.product.v1.ProductOuterClass
import com.ecommerce.springboot.product.v1.ProductServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService()
class ProductService(private val productRepository: ProductRepository) : ProductServiceGrpc.ProductServiceImplBase() {

    override fun getProducts(
        request: ProductOuterClass.GetProductsRequest,
        responseObserver: StreamObserver<ProductOuterClass.GetProductsResponse>
    ) {
        responseObserver.onNext(
            ProductOuterClass.GetProductsResponse.newBuilder()
                .addAllProducts(
                    productRepository.getProducts(request.skip.value, request.take.value).map { product ->
                        ProductOuterClass.Product.newBuilder()
                            .setId(product.id)
                            .setName(product.name)
                            .setDescription(product.description ?: "")
                            .setBrand(product.brand ?: "")
                            .addAllMediaUrls(product.mediaUrls)
                            .setMinPrice(product.minPrice.toDouble())
                            .setMaxPrice(product.maxPrice.toDouble())
                            .setRating(product.rating)
                            .setTotalSaleCount(product.totalSaleCount)
                            .build()
                    }
                )
                .build()
        )
        responseObserver.onCompleted()
    }
}
