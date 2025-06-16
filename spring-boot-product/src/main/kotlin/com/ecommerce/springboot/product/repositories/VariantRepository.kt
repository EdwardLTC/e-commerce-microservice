package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.OptionTypesTable
import com.ecommerce.springboot.product.database.OptionValuesTable
import com.ecommerce.springboot.product.database.VariantOptionValuesTable
import com.ecommerce.springboot.product.database.VariantOptionValuesTable.optionValue
import com.ecommerce.springboot.product.database.VariantOptionValuesTable.variant
import com.ecommerce.springboot.product.database.VariantsTable
import com.ecommerce.springboot.product.dto.CreateVariantDto
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
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

    companion object {
        data class Variant(
            val id: UUID,
            val sku: String?,
            val price: Double,
            val salePrice: Double?,
            val stock: Int,
            val status: String,
            val mediaUrl: String?,
            val weight: Double?,
            val dimensions: String?,
            val selectedOptions: MutableList<SelectedOption> = mutableListOf()
        )

        data class SelectedOption(
            val optionTypeId: UUID,
            val optionValueId: UUID,
        )
    }

    fun create(request: CreateVariantDto): UUID {
        productRepository.getById(request.productId)
            ?: throw IllegalArgumentException("Product with ID ${request.productId} not found")

        val optionValues =
            optionTypeRepository.getOptionValuesByIds(request.options).filter { it.productId == request.productId }

        if (optionValues.size != request.options.size) {
            val invalidIds = request.options - optionValues.map { it.id }
            throw IllegalArgumentException("Invalid option values: $invalidIds")
        }

        return transaction {
            val variantId = VariantsTable.insertAndGetId { row ->
                row[product] = request.productId
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

            productRepository.updateProduct(request.productId, true)

            return@transaction variantId
        }

    }

    fun getByProductId(productId: UUID): List<Variant> {
        val variantsMap = mutableMapOf<UUID, Variant>()

        VariantsTable
            .join(VariantOptionValuesTable, JoinType.LEFT, onColumn = VariantsTable.id, otherColumn = variant)
            .join(OptionValuesTable, JoinType.LEFT, onColumn = optionValue, otherColumn = OptionValuesTable.id)
            .join(
                OptionTypesTable,
                JoinType.LEFT,
                onColumn = OptionValuesTable.optionType,
                otherColumn = OptionTypesTable.id
            )
            .select(VariantsTable.columns + listOf(optionValue, OptionValuesTable.optionType))
            .where { VariantsTable.product eq productId }
            .forEach { row ->
                val variantId = row[VariantsTable.id].value
                val variant = variantsMap.getOrPut(variantId) {
                    Variant(
                        id = variantId,
                        sku = row[VariantsTable.sku],
                        price = row[VariantsTable.price].toDouble(),
                        salePrice = row[VariantsTable.salePrice]?.toDouble(),
                        stock = row[VariantsTable.stock],
                        status = row[VariantsTable.status].name,
                        mediaUrl = row[VariantsTable.mediaUrl],
                        weight = row[VariantsTable.weight]?.toDouble(),
                        dimensions = row[VariantsTable.dimensions],
                        selectedOptions = mutableListOf()
                    )
                }

                val optionValueId = row[optionValue]
                val optionTypeId = row[OptionValuesTable.optionType]

                if (optionValueId != null && optionTypeId != null) {
                    variant.selectedOptions.add(
                        SelectedOption(
                            optionTypeId = optionTypeId.value,
                            optionValueId = optionValueId.value
                        )
                    )
                }
            }
        return variantsMap.values.toList()
    }
}