package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.ProductsTable
import com.ecommerce.springboot.product.database.StockReversalItemsTable
import com.ecommerce.springboot.product.database.StockReversalsTable
import com.ecommerce.springboot.product.database.VariantsTable
import com.ecommerce.springboot.product.dto.ReverseStockDto
import com.ecommerce.springboot.product.repositories.VariantRepository.Companion.VariantWithProduct
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Repository
@Transactional
class StockRepository {
    companion object {
        data class ReverseStockResponse(
            val reversalId: UUID, val items: List<VariantWithProduct>
        )
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
                    it[stock] = stock.minus(quantity)
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
                    it.update(stock, stock.plus(quantity))
                }
            }

            StockReversalsTable.update({ StockReversalsTable.id eq reservationId }) {
                it[status] = StockReversalsTable.StockCheckinStatus.RELEASED
                it[releaseAt] = OffsetDateTime.now()
            }

            return@transaction items.size
        }
    }
}