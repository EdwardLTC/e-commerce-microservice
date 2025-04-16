package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.database.ProductStatus
import com.ecommerce.springboot.product.database.ProductTable
import com.ecommerce.springboot.product.database.StockStatus
import com.ecommerce.springboot.product.v1.ProductOuterClass
import com.ecommerce.springboot.product.v1.ProductServiceGrpc
import com.google.protobuf.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.springframework.grpc.server.service.GrpcService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@GrpcService
@Component
@Transactional
class ProductService : ProductServiceGrpc.ProductServiceImplBase() {

    companion object {
        private fun ResultRow.toProductProto(): ProductOuterClass.Product {
            return ProductOuterClass.Product.newBuilder().setId(this[ProductTable.id].toString())
                .setName(this[ProductTable.name])
                .setDescription(this[ProductTable.description])
                .setPrice(this[ProductTable.price].toDouble())
                .setQuantity(this[ProductTable.quantity])
                .setStockStatus(ProductOuterClass.StockStatus.forNumber(this[ProductTable.stockStatus].ordinal))
                .setStatus(ProductOuterClass.ProductStatus.forNumber(this[ProductTable.status].ordinal))
                .setTotalSales(this[ProductTable.totalSales])
                .setRating(this[ProductTable.rating].toDouble())
                .addAllCategoryIds(this[ProductTable.categoryIds].map { it.toString() })
                .addMediaUrls(this[ProductTable.mediaUrls].toString())
                .setCreatedAt(this[ProductTable.createdAt].toString())
                .build()
        }
    }

    override fun getProduct(
        request: ProductOuterClass.GetProductRequest,
        responseObserver: StreamObserver<ProductOuterClass.Product>
    ) {
        val baseConditions = mutableListOf<Op<Boolean>>()
        baseConditions += ProductTable.deletedAt.isNull()
        baseConditions += ProductTable.id eq runCatching { UUID.fromString(request.id) }.getOrElse {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Invalid product ID format: ${request.id}")
                    .asRuntimeException()
            )
            return
        }

        ProductTable.selectAll().where { baseConditions.reduce { acc, op -> acc and op } }.firstOrNull().let {
            if (it == null) {
                responseObserver.onError(
                    Status.INVALID_ARGUMENT
                        .withDescription("Product with ID ${request.id} not found")
                        .asRuntimeException()
                )
                return
            }

            responseObserver.onNext(it.toProductProto())
        }

        //TODO: Push this action to Kafka for analytics, recommendations, user behavior, etc... services

        responseObserver.onCompleted()
    }

    override fun getProducts(
        request: ProductOuterClass.GetProductsRequest,
        responseObserver: StreamObserver<ProductOuterClass.GetProductsResponse>
    ) {
        val baseConditions = mutableListOf<Op<Boolean>>()

        baseConditions += ProductTable.deletedAt.isNull()
        baseConditions += ProductTable.status eq ProductStatus.ACTIVE
        baseConditions += ProductTable.stockStatus eq StockStatus.IN_STOCK

        if (!request.categoryIdsList.isNullOrEmpty()) {
            baseConditions += ProductTable.categoryIds inList listOf(request.categoryIdsList.map { UUID.fromString(it) })
        }

        println("skip: ${request.skip.value}, take: ${request.take.value}, categoryIds: ${request.categoryIdsList}")

        ProductTable.selectAll().where { baseConditions.reduce { acc, op -> acc and op } }
            .offset(request.skip.value.toLong())
            .take(request.take.value)
            .toList().let {
                responseObserver.onNext(
                    ProductOuterClass.GetProductsResponse.newBuilder()
                        .addAllProducts(it.map { row -> row.toProductProto() }).build()
                )
            }

        responseObserver.onCompleted()
    }

    override fun createProduct(
        request: ProductOuterClass.CreateProductRequest,
        responseObserver: StreamObserver<ProductOuterClass.CreateProductResponse>
    ) {
        val newProduct = runCatching {
            ProductTable.insertAndGetId { it ->
                it[sellerId] = UUID.fromString(request.sellerId.value)
                it[name] = request.name.value
                it[description] = request.description.value
                it[price] = request.price.value.toBigDecimal()
                it[quantity] = request.quantity.value
                it[stockStatus] = StockStatus.IN_STOCK
                it[status] = ProductStatus.ACTIVE
                it[mediaUrls] = request.mediaUrlsList.map { it }
                it[categoryIds] = request.categoryIdsList.map { UUID.fromString(it) }
            }
        }.getOrElse {
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription("Failed to create product due to: ${it.message}")
                    .asRuntimeException()
            )
            return
        }

        //TODO: Push this post after creation to Kafka for analytics, recommendations, search, etc... services
        //TODO: Impl saga pattern to check sellerId exists and is active

        responseObserver.onNext(
            ProductOuterClass.CreateProductResponse.newBuilder()
                .setId(newProduct.value.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}
