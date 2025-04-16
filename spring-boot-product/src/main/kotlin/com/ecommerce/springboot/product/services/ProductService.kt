package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.database.ProductStatus
import com.ecommerce.springboot.product.database.ProductTable
import com.ecommerce.springboot.product.database.StockStatus
import com.ecommerce.springboot.product.v1.ProductOuterClass
import com.ecommerce.springboot.product.v1.ProductServiceGrpc
import com.google.protobuf.DoubleValue
import com.google.protobuf.Int32Value
import com.google.protobuf.StringValue
import com.google.protobuf.Timestamp
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
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
            return ProductOuterClass.Product.newBuilder().setId(StringValue.of(this[ProductTable.id].toString()))
                .setName(StringValue.of(this[ProductTable.name]))
                .setDescription(StringValue.of(this[ProductTable.description]))
                .setPrice(DoubleValue.of(this[ProductTable.price].toDouble()))
                .setQuantity(Int32Value.of(this[ProductTable.quantity]))
                .setStockStatus(ProductOuterClass.StockStatus.forNumber(this[ProductTable.stockStatus].ordinal))
                .setStatus(ProductOuterClass.ProductStatus.forNumber(this[ProductTable.status].ordinal))
                .setTotalSales(Int32Value.of(this[ProductTable.totalSales]))
                .setRating(DoubleValue.of(this[ProductTable.rating].toDouble())).setCreatedAt(
                    Timestamp.newBuilder().setNanos(this[ProductTable.createdAt].nanosecondsOfSecond)
                        .setSeconds(this[ProductTable.createdAt].epochSeconds).build()
                ).addAllCategoryIds(this[ProductTable.categoryIds].map { it.toString() })
                .addMediaUrls(this[ProductTable.mediaUrls].toString()).build()
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

        if (request.categoryIdsList.isNotEmpty()) {
            baseConditions += ProductTable.categoryIds inList listOf(request.categoryIdsList.map { UUID.fromString(it) })
        }

        ProductTable.selectAll().where { baseConditions.reduce { acc, op -> acc and op } }
            .offset(request.skip.value.toLong())
            .limit(request.take.value)
            .toList().let {
                responseObserver.onNext(
                    ProductOuterClass.GetProductsResponse.newBuilder()
                        .addAllProducts(it.map { row -> row.toProductProto() }).build()
                )
            }

        responseObserver.onCompleted()
    }
}
