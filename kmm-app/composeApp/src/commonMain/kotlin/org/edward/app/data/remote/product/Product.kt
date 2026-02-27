package org.edward.app.data.remote.product

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String? = null,
    val name: String,
    val price: Double,
    val description: String,
    val brand: String,
    val stock: Int,
    val mediaUrls: List<String> = emptyList()
)

@Serializable
data class ProductDetail(
    val id: String? = null,
    val name: String,
    val price: Double,
    val description: String,
    val brand: String,
    val stock: Int,
    val optionTypes: List<OptionType> = emptyList(),
    val mediaUrls: List<String> = emptyList(),
    val variants: List<Variant> = emptyList()
)

@Serializable
data class CreateProductOptionTypeRequest(
    val name: String,
    val displayOrder: Int? = null
)

@Serializable
data class OptionType(
    val id: String? = null,
    val name: String,
    val displayOrder: Int? = null
)

@Serializable
data class CreateProductOptionValueRequest(
    val value: String,
    val mediaUrl: String? = null,
    val displayOrder: Int? = null
)

@Serializable
data class OptionValue(
    val id: String? = null,
    val value: String,
    val mediaUrl: String? = null,
    val displayOrder: Int? = null
)

@Serializable
data class CreateProductVariantRequest(
    val sku: String,
    val price: Double,
    val stock: Int,
    val mediaUrl: String? = null,
    val options: List<String> = emptyList()
)

@Serializable
data class Variant(
    val id: String? = null,
    val sku: String,
    val price: Double? = null,
    val stock: Int? = null,
    val mediaUrl: String? = null,
    val options: List<String> = emptyList()
)
