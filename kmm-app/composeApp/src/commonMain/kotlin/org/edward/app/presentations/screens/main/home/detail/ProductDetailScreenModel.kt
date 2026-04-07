package org.edward.app.presentations.screens.main.home.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.edward.app.data.local.CartItem
import org.edward.app.data.local.CartRepository
import org.edward.app.data.remote.product.ProductDetail
import org.edward.app.data.remote.product.ProductRepository
import org.edward.app.data.remote.product.Variant
import org.edward.app.data.utils.AsyncResult

class ProductDetailScreenModel(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ScreenModel {

    data class UiState(
        val loading: Boolean = true,
        val product: ProductDetail? = null,
        val selectedOptions: Map<String, String> = emptyMap(),
        val quantity: Int = 1,
        val error: String? = null,
        val addedToCart: Boolean = false,
    ) {
        val selectedVariant: Variant?
            get() {
                val p = product ?: return null
                if (selectedOptions.size < p.optionTypes.size) return null
                return p.variants.firstOrNull { variant ->
                    variant.selectedOptions.all { so ->
                        selectedOptions[so.optionTypeId] == so.optionValueId
                    }
                }
            }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadProduct(productId: String) {
        screenModelScope.launch {
            _uiState.value = UiState(loading = true)
            when (val result = productRepository.getProduct(productId)) {
                is AsyncResult.Success -> {
                    val product = result.data
                    val initialOptions = initialSelections(product)
                    _uiState.value = UiState(
                        loading = false,
                        product = product,
                        selectedOptions = initialOptions,
                    )
                }

                is AsyncResult.Error -> {
                    _uiState.value = UiState(
                        loading = false,
                        error = result.displayMessage ?: "Failed to load product"
                    )
                }
            }
        }
    }

    fun selectOption(optionTypeId: String, optionValueId: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            selectedOptions = state.selectedOptions + (optionTypeId to optionValueId),
            addedToCart = false,
        )
    }

    /**
     * Returns whether [optionValueId] for [optionTypeId] has at least one
     * in-stock variant given the currently selected values of all *other* option types.
     */
    fun isOptionValueAvailable(optionTypeId: String, optionValueId: String): Boolean {
        val product = _uiState.value.product ?: return false
        val otherSelections = _uiState.value.selectedOptions.filterKeys { it != optionTypeId }
        return product.variants.any { variant ->
            variant.isAvailable &&
                variant.selectedOptions.any { it.optionTypeId == optionTypeId && it.optionValueId == optionValueId } &&
                otherSelections.all { (typeId, valId) ->
                    variant.selectedOptions.any { it.optionTypeId == typeId && it.optionValueId == valId }
                }
        }
    }

    fun updateQuantity(qty: Int) {
        if (qty in 1..99) {
            _uiState.value = _uiState.value.copy(quantity = qty)
        }
    }

    fun addToCart() {
        val state = _uiState.value
        val product = state.product ?: return
        val variant = state.selectedVariant ?: return

        if (!variant.isAvailable) return

        val optionDesc = buildVariantDescription(product, variant)

        cartRepository.addItem(
            CartItem(
                variantId = variant.id,
                productId = product.id,
                productName = product.name,
                variantDescription = optionDesc,
                price = variant.effectivePrice,
                quantity = state.quantity,
                imageUrl = variant.mediaUrl ?: product.mediaUrls.firstOrNull()
            )
        )
        _uiState.value = state.copy(addedToCart = true)
    }

    private fun initialSelections(product: ProductDetail): Map<String, String> {
        val first = product.variants.firstOrNull { it.isAvailable }
            ?: product.variants.firstOrNull()
            ?: return emptyMap()
        return first.selectedOptions.associate { it.optionTypeId to it.optionValueId }
    }

    private fun buildVariantDescription(product: ProductDetail, variant: Variant): String {
        return variant.selectedOptions.mapNotNull { selected ->
            val optType = product.optionTypes.find { it.id == selected.optionTypeId }
            val optVal = optType?.optionValues?.find { it.id == selected.optionValueId }
            if (optType != null && optVal != null) "${optType.name}: ${optVal.value}" else null
        }.joinToString(", ").ifEmpty { variant.sku }
    }
}
