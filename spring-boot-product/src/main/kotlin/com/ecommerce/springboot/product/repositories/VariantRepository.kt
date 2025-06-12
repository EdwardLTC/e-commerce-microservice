package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.VariantOptionValuesTable
import com.ecommerce.springboot.product.database.VariantOptionValuesTable.optionValue
import com.ecommerce.springboot.product.database.VariantOptionValuesTable.variant
import com.ecommerce.springboot.product.database.VariantsTable
import com.ecommerce.springboot.product.models.CreateVariantRequest
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
class VariantRepository(
    private val productRepository: ProductRepository,
    private val optionTypeRepository: OptionTypeRepository
) {

    fun create(productId: UUID, request: CreateVariantRequest): UUID {
        productRepository.getById(productId) ?: throw IllegalArgumentException("Product with ID $productId not found")
        val optionValues = optionTypeRepository.getOptionValuesByIds(request.options)

        val correctOptionValueMatchProductId = optionValues.filter { it.productId == productId }.map { it.value }

        if (correctOptionValueMatchProductId.size != request.options.size) {
            throw IllegalArgumentException("Some option values do not match the product ID $productId")
        }

        return transaction {
            val variantId = VariantsTable.insertAndGetId { row ->
                row[product] = productId
                row[sku] = request.sku
                row[price] = request.price
                row[stock] = request.stock
                row[mediaUrl] = request.mediaUrl
                row[weight] = request.weight
                row[dimensions] = request.dimensions
                row[status] = VariantsTable.VariantStatus.ACTIVE
            }.value

            VariantOptionValuesTable.batchInsert(request.options) { option ->
                this[variant] = variantId
                this[optionValue] = option
            }

            productRepository.updateProduct(productId, true)

            return@transaction variantId
        }
        
    }
}