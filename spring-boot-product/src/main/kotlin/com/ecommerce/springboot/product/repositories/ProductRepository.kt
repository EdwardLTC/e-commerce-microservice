package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.clients.UserServiceClient
import com.ecommerce.springboot.product.database.ProductsTable
import com.ecommerce.springboot.product.database.VariantsTable
import com.ecommerce.springboot.product.dto.CreateProductRequestDto
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.core.min
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Repository
@Transactional
class ProductRepository(private val userServiceClient: UserServiceClient) {

    companion object {
        data class GetProductByIdResponse(
            val id: UUID,
            val name: String,
            val description: String?,
            val brand: String?,
            val mediaUrls: List<String>,
            val isActive: Boolean
        )

        data class GetProductResponse(
            val id: UUID,
            val name: String,
            val description: String?,
            val brand: String?,
            val mediaUrls: List<String>,
            val minPrice: BigDecimal,
            val maxPrice: BigDecimal,
            val rating: Double,
            val totalSaleCount: Int,
        )
    }

    fun create(createProduct: CreateProductRequestDto): UUID {
        userServiceClient.get(createProduct.sellerId.toString())
        
        return ProductsTable.insertAndGetId { row ->
            row[sellerId] = createProduct.sellerId.toString()
            row[name] = createProduct.name
            row[description] = createProduct.description
            row[brand] = createProduct.brand
            row[mediaUrls] = createProduct.mediaUrls
            row[isActive] = false
        }.value
    }

    fun getById(id: UUID): GetProductByIdResponse? {
        val row = ProductsTable.select(
            ProductsTable.id,
            ProductsTable.name,
            ProductsTable.description,
            ProductsTable.brand,
            ProductsTable.mediaUrls,
            ProductsTable.isActive,
            ProductsTable.createdAt,
            ProductsTable.updatedAt,
        ).where { ProductsTable.id eq id }.singleOrNull() ?: return null

        return GetProductByIdResponse(
            id = row[ProductsTable.id].value,
            name = row[ProductsTable.name],
            description = row[ProductsTable.description],
            brand = row[ProductsTable.brand],
            mediaUrls = row[ProductsTable.mediaUrls],
            isActive = row[ProductsTable.isActive],
        )
    }

    fun getProducts(skip: Int = 0, take: Int = 10): List<GetProductResponse> {
        val selectFields = listOf(
            ProductsTable.id,
            ProductsTable.name,
            ProductsTable.description,
            ProductsTable.brand,
            ProductsTable.mediaUrls,
            ProductsTable.averageRating,
            ProductsTable.totalSaleCount,
            VariantsTable.price.min(),
            VariantsTable.price.max()
        )

        return ProductsTable.join(
            otherTable = VariantsTable,
            joinType = JoinType.LEFT,
            onColumn = ProductsTable.id,
            otherColumn = VariantsTable.product,
        ).select(selectFields)
            .where { ProductsTable.isActive eq true }
            .groupBy(ProductsTable.id)
            .offset(skip.toLong()).limit(take).toList().map { row ->
                GetProductResponse(
                    id = row[ProductsTable.id].value,
                    name = row[ProductsTable.name],
                    description = row[ProductsTable.description],
                    brand = row[ProductsTable.brand],
                    mediaUrls = row[ProductsTable.mediaUrls],
                    minPrice = row[VariantsTable.price.min()] ?: BigDecimal.ZERO,
                    maxPrice = row[VariantsTable.price.max()] ?: BigDecimal.ZERO,
                    rating = row[ProductsTable.averageRating],
                    totalSaleCount = row[ProductsTable.totalSaleCount]
                )
            }

    }

    fun updateProduct(id: UUID, isActive: Boolean): Boolean {
        val updatedRows = ProductsTable.update({ ProductsTable.id eq id }) {
            it[this.isActive] = isActive
        }
        return updatedRows > 0
    }
}