package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.*
import com.ecommerce.springboot.product.database.VariantOptionValuesTable.optionValue
import com.ecommerce.springboot.product.database.VariantOptionValuesTable.variant
import com.ecommerce.springboot.product.dto.CreateVariantDto
import com.ecommerce.springboot.product.dto.ReverseStockDto
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.leftJoin
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
class VariantRepository(
    private val productRepository: ProductRepository, private val optionTypeRepository: OptionRepository
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

        data class VariantWithProduct(
            val id: UUID,
            val sku: String?,
            val price: Double,
            val salePrice: Double,
            val stock: Int,
            val status: String,
            val mediaUrl: String?,
            val productId: UUID,
            val productName: String,
        )

        data class ReverseStockResponse(
            val reversalId: UUID, val items: List<VariantWithProduct>
        )
    }

    fun create(request: CreateVariantDto): UUID {
        productRepository.getById(request.productId)
            ?: throw IllegalArgumentException("Product with ID ${request.productId} not found")

        val optionValues =
            optionTypeRepository.getOptionValuesByIds(request.options).filter { it.productId == request.productId }

        if (optionValues.size != request.options.size) {
            val invalidIds = request.options - optionValues.map { it.id }.toSet()
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

        VariantsTable.leftJoin(VariantOptionValuesTable, onColumn = { id }, otherColumn = { variant })
            .leftJoin(OptionValuesTable, onColumn = { optionValue }, otherColumn = { id })
            .leftJoin(OptionTypesTable, onColumn = { OptionValuesTable.optionType }, otherColumn = { id })
            .select(VariantsTable.columns + listOf(optionValue, OptionValuesTable.optionType))
            .where { VariantsTable.product eq productId }.forEach { row ->
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

                val optionValueId = row.getOrNull(optionValue)
                val optionTypeId = row.getOrNull(OptionValuesTable.optionType)

                if (optionValueId != null && optionTypeId != null) {
                    variant.selectedOptions.add(
                        SelectedOption(
                            optionTypeId = optionTypeId.value, optionValueId = optionValueId.value
                        )
                    )
                }
            }
        return variantsMap.values.toList()
    }

    fun reserveStock(request: ReverseStockDto): ReverseStockResponse {
        return transaction {
            val reversalId = StockReversalsTable.insertAndGetId {
                it[status] = StockReversalsTable.StockCheckinStatus.PENDING
            }.value

            val variants = request.map { item ->
                val variantId = item.variantId
                val quantity = item.quantity

                val effectedRow = VariantsTable.update(
                    where = { (VariantsTable.id eq variantId) and (VariantsTable.stock greaterEq quantity) }) {
                    with(SqlExpressionBuilder) {
                        it.update(stock, stock - quantity)
                    }
                }

                if (effectedRow == 0) {
                    throw IllegalArgumentException("Insufficient stock for variant ID: $variantId")
                }

                StockReversalItemsTable.insert {
                    it[this.reversal] = reversalId
                    it[this.variant] = variantId
                    it[this.quantity] = quantity
                }

                val joinedRow = (VariantsTable innerJoin ProductsTable).select(
                    VariantsTable.id,
                    VariantsTable.sku,
                    VariantsTable.price,
                    VariantsTable.salePrice,
                    VariantsTable.stock,
                    VariantsTable.status,
                    VariantsTable.mediaUrl,
                    VariantsTable.product,
                    ProductsTable.name
                ).where { VariantsTable.id eq variantId }.single()

                VariantWithProduct(
                    id = joinedRow[VariantsTable.id].value,
                    sku = joinedRow[VariantsTable.sku],
                    price = joinedRow[VariantsTable.price].toDouble(),
                    salePrice = joinedRow[VariantsTable.salePrice]?.toDouble() ?: 0.0,
                    stock = joinedRow[VariantsTable.stock],
                    status = joinedRow[VariantsTable.status].name,
                    mediaUrl = joinedRow[VariantsTable.mediaUrl],
                    productId = joinedRow[ProductsTable.id].value,
                    productName = joinedRow[ProductsTable.name]

                )
            }

            return@transaction ReverseStockResponse(
                reversalId = reversalId,
                items = variants,
            )

        }
    }

    fun releaseStock(reservationId: UUID): Int {
        return transaction {
            val items = StockReversalItemsTable.selectAll().where {
                StockReversalItemsTable.reversal eq reservationId
            }.associate { row ->
                row[StockReversalItemsTable.variant] to row[StockReversalItemsTable.quantity]
            }

            items.forEach { (variantId, quantity) ->
                VariantsTable.update({ VariantsTable.id eq variantId }) {
                    with(SqlExpressionBuilder) {
                        it.update(stock, stock + quantity)
                    }
                }
            }

            StockReversalsTable.update({ StockReversalsTable.id eq reservationId }) {
                it[status] = StockReversalsTable.StockCheckinStatus.RELEASED
                it[releaseAt] = CurrentTimestamp
            }

            return@transaction items.size
        }
    }

}