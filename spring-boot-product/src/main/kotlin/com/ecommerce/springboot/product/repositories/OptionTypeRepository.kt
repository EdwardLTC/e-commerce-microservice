package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.OptionTypesTable
import com.ecommerce.springboot.product.database.OptionValuesTable
import com.ecommerce.springboot.product.dto.CreateOptionTypeDto
import com.ecommerce.springboot.product.dto.CreateOptionValueDto
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
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
            val productId: String,
            val name: String,
            val displayOrder: Short,
            val optionValues: List<OptionValue> = emptyList()
        )

        data class OptionValue(
            val id: String, val value: String, val mediaUrl: String? = null, val displayOrder: Short = 1
        )
    }

    fun create(request: CreateOptionTypeDto): OptionType {
        productRepository.getById(request.productId)
            ?: throw IllegalArgumentException("Product with ID ${request.productId} not found")

        return transaction {
            val optionType = OptionTypesTable.insertReturning {
                it[product] = request.productId
                it[name] = request.name
                it[displayOrder] = request.displayOrder.toShort()
            }.map {
                OptionType(
                    id = it[OptionTypesTable.id].value.toString(),
                    productId = it[OptionTypesTable.product].value.toString(),
                    name = it[OptionTypesTable.name],
                    displayOrder = it[OptionTypesTable.displayOrder],
                    optionValues = emptyList()
                )
            }.first()

            return@transaction optionType
        }
    }

    fun createOptionValue(request: CreateOptionValueDto): UUID {
        OptionTypesTable.select(OptionTypesTable.id).where { OptionTypesTable.id eq request.optionTypeId }
            .firstOrNull()
            ?: throw IllegalArgumentException("Option type with ID ${request.optionTypeId} not found")

        return OptionValuesTable.insertAndGetId {
            it[this.optionType] = request.optionTypeId
            it[this.value] = request.value
            it[this.mediaUrl] = request.mediaUrl
            it[this.displayOrder] = request.displayOrder
        }.value

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
            .groupBy { row -> row[OptionTypesTable.id].value.toString() }.map { (id, rows) ->
                val optionType = rows.first()
                OptionType(
                    id = id,
                    productId = optionType[OptionTypesTable.product].value.toString(),
                    name = optionType[OptionTypesTable.name],
                    displayOrder = optionType[OptionTypesTable.displayOrder],
                    optionValues = rows.filter { it[OptionValuesTable.id] != null }.map { row ->
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