package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.StockReversalItemsTable
import com.ecommerce.springboot.product.database.StockReversalsTable
import com.ecommerce.springboot.product.database.VariantsTable
import com.ecommerce.springboot.product.dto.ReserveStock
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Repository
@Transactional
class StockRepository {
    fun reserveStock(request: List<ReserveStock>, orderId: String): UUID {

        val isExisting = StockReversalsTable.selectAll().where { StockReversalsTable.orderId eq orderId }.singleOrNull()

        if (isExisting != null) {
            return isExisting[StockReversalsTable.id].value
        }

        return transaction {
            val reversalId = StockReversalsTable.insertAndGetId {
                it[status] = StockReversalsTable.StockCheckinStatus.REVERSED
                it[this.orderId] = orderId
            }.value

            request.map { item ->
                val variantId = item.variantId
                val quantity = item.quantity
                val unitPrice = BigDecimal.valueOf(item.unitPrice)

                val effectedRow = VariantsTable.update(
                    where = { (VariantsTable.id eq variantId) and (VariantsTable.stock greaterEq quantity) and (VariantsTable.price eq unitPrice) }) {
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
            }

            return@transaction reversalId
        }
    }

    fun releaseStock(orderId: String): Int {
        val reversal = StockReversalsTable.selectAll().where { StockReversalsTable.orderId eq orderId }.singleOrNull()

        if (reversal == null) {
            throw IllegalArgumentException("Stock reversal not exist for: $orderId")
        }

        return transaction {
            val items = StockReversalItemsTable.selectAll().where {
                StockReversalItemsTable.reversal eq reversal[StockReversalsTable.id]
            }.associate { row ->
                row[StockReversalItemsTable.variant] to row[StockReversalItemsTable.quantity]
            }

            items.forEach { (variantId, quantity) ->
                VariantsTable.update({ VariantsTable.id eq variantId }) {
                    it.update(stock, stock.plus(quantity))
                }
            }

            StockReversalsTable.update({ StockReversalsTable.id eq reversal[StockReversalsTable.id] }) {
                it[status] = StockReversalsTable.StockCheckinStatus.RELEASED
            }

            return@transaction items.size
        }
    }
}