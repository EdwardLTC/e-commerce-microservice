package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.OptionTypesTable
import com.ecommerce.springboot.product.database.OptionValuesTable
import com.ecommerce.springboot.product.models.CreateOptionTypeRequest
import com.ecommerce.springboot.product.models.OptionType
import com.ecommerce.springboot.product.models.OptionValue
import com.ecommerce.springboot.product.models.OptionValueWithProductId
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

    fun create(productId: UUID, request: CreateOptionTypeRequest): OptionType {
        productRepository.getById(productId) ?: throw IllegalArgumentException("Product with ID $productId not found")

        return transaction {
            val optionType = OptionTypesTable.insertReturning {
                it[product] = productId
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

    fun getOptionValuesByIds(optionTypeIds: List<UUID>): List<OptionValueWithProductId> {
        return (OptionValuesTable innerJoin OptionTypesTable)
            .select(
                OptionValuesTable.id,
                OptionValuesTable.value,
                OptionValuesTable.mediaUrl,
                OptionValuesTable.displayOrder,
                OptionTypesTable.product
            ).where { OptionValuesTable.id inList optionTypeIds.toList() }
            .map { row ->
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