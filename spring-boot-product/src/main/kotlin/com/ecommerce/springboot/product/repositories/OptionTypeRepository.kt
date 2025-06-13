package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.OptionTypesTable
import com.ecommerce.springboot.product.database.OptionValuesTable
import com.ecommerce.springboot.product.dto.CreateOptionTypeRequestDto
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Transactional
class OptionTypeRepository(private val productRepository: ProductRepository) {
    companion object {
        data class OptionValueWithProductId(
            val id: String,
            val value: String,
            val mediaUrl: String? = null,
            val displayOrder: Short = 1,
            val productId: UUID
        )

        data class OptionType(
            val id: String,
            val productId: UUID,
            val name: String,
            val displayOrder: Short,
            var optionValues: List<OptionValue> = emptyList()
        )

        data class OptionValue(
            val id: String, val value: String, val mediaUrl: String? = null, val displayOrder: Short = 1
        )
    }

    fun create(productId: UUID, request: CreateOptionTypeRequestDto): OptionType {
        productRepository.getById(productId) ?: throw IllegalArgumentException("Product with ID $productId not found")

        return transaction {
            val optionType = OptionTypesTable.insertReturning {
                it[product] = productId
                it[name] = request.name
                it[displayOrder] = request.displayOrder.toShort()
            }.map {
                OptionType(
                    id = it[OptionTypesTable.id].value.toString(),
                    productId = it[OptionTypesTable.product].value,
                    name = it[OptionTypesTable.name],
                    displayOrder = it[OptionTypesTable.displayOrder],
                    optionValues = emptyList()
                )
            }.first()

            val optionValues = OptionValuesTable.batchInsert(request.optionValues) { optionValue ->
                this[OptionValuesTable.optionType] = UUID.fromString(optionType.id)
                this[OptionValuesTable.value] = optionValue.value
                this[OptionValuesTable.mediaUrl] = optionValue.mediaUrl
                this[OptionValuesTable.displayOrder] = optionValue.displayOrder.toShort()
            }.map {
                OptionValue(
                    id = it[OptionValuesTable.id].value.toString(),
                    value = it[OptionValuesTable.value],
                    mediaUrl = it[OptionValuesTable.mediaUrl],
                    displayOrder = it[OptionValuesTable.displayOrder]
                )
            }.toList()

            optionType.optionValues = optionValues

            return@transaction optionType
        }
    }

    fun getByProductId(productId: UUID): List<OptionType> {
        val selectFields = listOf(
            OptionTypesTable.id,
            OptionTypesTable.product,
            OptionTypesTable.name,
            OptionTypesTable.displayOrder,
            OptionValuesTable.id,
            OptionValuesTable.value,
            OptionValuesTable.mediaUrl,
            OptionValuesTable.displayOrder
        )

        return OptionTypesTable.join(
            OptionValuesTable,
            JoinType.LEFT,
            OptionTypesTable.id,
            OptionValuesTable.optionType
        ).select(selectFields).where { OptionTypesTable.product eq productId }.toList()
            .groupBy { row ->
                row[OptionTypesTable.id].value.toString()
            }.map { (id, rows) ->
                val optionType = rows.first()
                OptionType(
                    id = id,
                    productId = optionType[OptionTypesTable.product].value,
                    name = optionType[OptionTypesTable.name],
                    displayOrder = optionType[OptionTypesTable.displayOrder],
                    optionValues = rows.map { row ->
                        OptionValue(
                            id = row[OptionValuesTable.id].value.toString(),
                            value = row[OptionValuesTable.value],
                            mediaUrl = row[OptionValuesTable.mediaUrl],
                            displayOrder = row[OptionValuesTable.displayOrder]
                        )
                    })

            }
    }

    fun getOptionValuesByIds(optionTypeIds: List<UUID>): List<OptionValueWithProductId> {
        val selectFields = listOf(
            OptionValuesTable.id,
            OptionValuesTable.value,
            OptionValuesTable.mediaUrl,
            OptionValuesTable.displayOrder,
            OptionTypesTable.product
        )

        return OptionValuesTable.join(
            OptionTypesTable,
            joinType = JoinType.INNER,
            onColumn = OptionValuesTable.optionType,
            otherColumn = OptionTypesTable.id
        ).select(selectFields).where { OptionValuesTable.id inList optionTypeIds.toList() }.map { row ->
            OptionValueWithProductId(
                id = row[OptionValuesTable.id].value.toString(),
                value = row[OptionValuesTable.value],
                mediaUrl = row[OptionValuesTable.mediaUrl],
                displayOrder = row[OptionValuesTable.displayOrder],
                productId = row[OptionTypesTable.product].value
            )
        }
    }
}