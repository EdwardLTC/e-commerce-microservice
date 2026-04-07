package org.edward.app.data.remote.product

import kotlinx.serialization.Serializable
import org.edward.app.data.utils.formatPrice

@Serializable
data class GetProductsResponse(
    val products: List<Product> = emptyList()
)

@Serializable
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val brand: String = "",
    val minPrice: Double = 0.0,
    val maxPrice: Double = 0.0,
    val totalSaleCount: Int = 0,
    val rating: Double = 0.0,
    val mediaUrls: List<String> = emptyList()
) {
    val displayPrice: String
        get() = if (minPrice == maxPrice || maxPrice == 0.0) {
            "$${minPrice.formatPrice()}"
        } else {
            "$${minPrice.formatPrice()} - $${maxPrice.formatPrice()}"
        }

    val thumbnailUrl: String?
        get() = mediaUrls.firstOrNull()
}

@Serializable
data class ProductDetail(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val brand: String = "",
    val totalSaleCount: Int = 0,
    val averageRating: Double = 0.0,
    val mediaUrls: List<String> = emptyList(),
    val optionTypes: List<OptionType> = emptyList(),
    val variants: List<Variant> = emptyList()
)

@Serializable
data class OptionType(
    val id: String = "",
    val name: String = "",
    val displayOrder: Int = 0,
    val optionValues: List<OptionValue> = emptyList()
)

@Serializable
data class OptionValue(
    val id: String = "",
    val value: String = "",
    val mediaUrl: String? = null,
    val displayOrder: Int = 0
)

@Serializable
data class Variant(
    val id: String = "",
    val sku: String = "",
    val price: Double = 0.0,
    val salePrice: Double = 0.0,
    val stock: Int = 0,
    val status: String = "",
    val mediaUrl: String? = null,
    val selectedOptions: List<SelectedOption> = emptyList()
) {
    val isAvailable: Boolean get() = stock > 0
    val hasDiscount: Boolean get() = salePrice > 0 && salePrice < price
    val effectivePrice: Double get() = if (hasDiscount) salePrice else price
}

@Serializable
data class SelectedOption(
    val optionTypeId: String = "",
    val optionValueId: String = ""
)

@Serializable
data class CreateProductRequest(
    val name: String,
    val description: String,
    val brand: String,
    val mediaUrls: List<String> = emptyList()
)

@Serializable
data class CreateProductResponse(val id: String)

@Serializable
data class CreateProductOptionTypeRequest(
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
data class CreateProductVariantRequest(
    val sku: String,
    val price: Double,
    val stock: Int,
    val mediaUrl: String? = null,
    val options: List<String> = emptyList()
)
