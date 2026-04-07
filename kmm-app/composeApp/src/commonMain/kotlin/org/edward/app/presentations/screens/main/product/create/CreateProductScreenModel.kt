package org.edward.app.presentations.screens.main.product.create

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.edward.app.data.remote.product.CreateProductOptionTypeRequest
import org.edward.app.data.remote.product.CreateProductOptionValueRequest
import org.edward.app.data.remote.product.CreateProductRequest
import org.edward.app.data.remote.product.CreateProductVariantRequest
import org.edward.app.data.remote.product.ProductRepository
import org.edward.app.data.utils.AsyncResult

class CreateProductScreenModel(
    private val productRepository: ProductRepository,
) : ScreenModel {

    companion object {
        const val TOTAL_STEPS = 4
    }

    // --- Local draft models ---

    data class DraftOptionType(
        val name: String = "",
        val serverId: String? = null,
        val values: List<DraftOptionValue> = emptyList(),
    )

    data class DraftOptionValue(
        val value: String = "",
        val serverId: String? = null,
    )

    data class DraftVariant(
        val sku: String = "",
        val price: String = "",
        val stock: String = "",
        val selectedOptionValueIds: List<String> = emptyList(),
    )

    data class UiState(
        val currentStep: Int = 0,
        val isSubmitting: Boolean = false,
        val error: String? = null,
        val completed: Boolean = false,

        // Step 0: Product Info
        val name: String = "",
        val brand: String = "",
        val description: String = "",
        val imageUrl: String = "",
        val imageUrls: List<String> = emptyList(),

        // Step 1: Option Types
        val optionTypes: List<DraftOptionType> = emptyList(),
        val newOptionTypeName: String = "",

        // Step 2: Option Values (populated after step 1)
        val newOptionValueInputs: Map<Int, String> = emptyMap(),

        // Step 3: Variants
        val variants: List<DraftVariant> = emptyList(),

        // Created product ID (set after step 0 API call)
        val createdProductId: String? = null,
    ) {
        val canProceed: Boolean
            get() = when (currentStep) {
                0 -> name.isNotBlank() && brand.isNotBlank()
                1 -> true
                2 -> true
                3 -> {
                    val typesWithValues = optionTypes.filter { ot ->
                        ot.values.any { it.serverId != null }
                    }
                    variants.isNotEmpty() && variants.all { v ->
                        v.sku.isNotBlank()
                            && v.price.toDoubleOrNull() != null
                            && v.stock.toIntOrNull() != null
                            && typesWithValues.all { ot ->
                                val valueIds = ot.values.mapNotNull { it.serverId }.toSet()
                                v.selectedOptionValueIds.any { it in valueIds }
                            }
                    }
                }
                else -> false
            }

        val stepTitle: String
            get() = when (currentStep) {
                0 -> "Product Info"
                1 -> "Option Types"
                2 -> "Option Values"
                3 -> "Variants"
                else -> ""
            }

        val stepSubtitle: String
            get() = when (currentStep) {
                0 -> "Basic product details"
                1 -> "Add attributes like Size, Color"
                2 -> "Add values for each attribute"
                3 -> "Define SKU, price and stock"
                else -> ""
            }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    // --- Step 0: Product Info ---
    fun onNameChange(v: String) { _uiState.value = _uiState.value.copy(name = v, error = null) }
    fun onBrandChange(v: String) { _uiState.value = _uiState.value.copy(brand = v, error = null) }
    fun onDescriptionChange(v: String) { _uiState.value = _uiState.value.copy(description = v, error = null) }
    fun onImageUrlChange(v: String) { _uiState.value = _uiState.value.copy(imageUrl = v, error = null) }

    fun addImageUrl() {
        val url = _uiState.value.imageUrl.trim()
        if (url.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                imageUrls = _uiState.value.imageUrls + url,
                imageUrl = "",
            )
        }
    }

    fun removeImageUrl(index: Int) {
        _uiState.value = _uiState.value.copy(
            imageUrls = _uiState.value.imageUrls.filterIndexed { i, _ -> i != index }
        )
    }

    // --- Step 1: Option Types ---
    fun onNewOptionTypeNameChange(v: String) {
        _uiState.value = _uiState.value.copy(newOptionTypeName = v, error = null)
    }

    fun addOptionType() {
        val name = _uiState.value.newOptionTypeName.trim()
        if (name.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
                optionTypes = _uiState.value.optionTypes + DraftOptionType(name = name),
                newOptionTypeName = "",
            )
        }
    }

    fun removeOptionType(index: Int) {
        _uiState.value = _uiState.value.copy(
            optionTypes = _uiState.value.optionTypes.filterIndexed { i, _ -> i != index }
        )
    }

    // --- Step 2: Option Values ---
    fun onNewOptionValueChange(optionTypeIndex: Int, v: String) {
        _uiState.value = _uiState.value.copy(
            newOptionValueInputs = _uiState.value.newOptionValueInputs + (optionTypeIndex to v),
            error = null,
        )
    }

    fun addOptionValue(optionTypeIndex: Int) {
        val input = _uiState.value.newOptionValueInputs[optionTypeIndex]?.trim() ?: return
        if (input.isBlank()) return
        val types = _uiState.value.optionTypes.toMutableList()
        val current = types[optionTypeIndex]
        types[optionTypeIndex] = current.copy(values = current.values + DraftOptionValue(value = input))
        _uiState.value = _uiState.value.copy(
            optionTypes = types,
            newOptionValueInputs = _uiState.value.newOptionValueInputs + (optionTypeIndex to ""),
        )
    }

    fun removeOptionValue(optionTypeIndex: Int, valueIndex: Int) {
        val types = _uiState.value.optionTypes.toMutableList()
        val current = types[optionTypeIndex]
        types[optionTypeIndex] = current.copy(
            values = current.values.filterIndexed { i, _ -> i != valueIndex }
        )
        _uiState.value = _uiState.value.copy(optionTypes = types)
    }

    // --- Step 3: Variants ---
    fun addVariant() {
        _uiState.value = _uiState.value.copy(
            variants = _uiState.value.variants + DraftVariant()
        )
    }

    fun removeVariant(index: Int) {
        _uiState.value = _uiState.value.copy(
            variants = _uiState.value.variants.filterIndexed { i, _ -> i != index }
        )
    }

    fun onVariantSkuChange(index: Int, v: String) = updateVariant(index) { it.copy(sku = v) }
    fun onVariantPriceChange(index: Int, v: String) = updateVariant(index) { it.copy(price = v) }
    fun onVariantStockChange(index: Int, v: String) = updateVariant(index) { it.copy(stock = v) }

    fun selectVariantOption(variantIndex: Int, optionTypeIndex: Int, optionValueId: String) {
        updateVariant(variantIndex) { variant ->
            val siblingIds = _uiState.value.optionTypes.getOrNull(optionTypeIndex)
                ?.values?.mapNotNull { it.serverId }?.toSet() ?: emptySet()
            val filtered = variant.selectedOptionValueIds.filter { it !in siblingIds }
            variant.copy(selectedOptionValueIds = filtered + optionValueId)
        }
    }

    private fun updateVariant(index: Int, transform: (DraftVariant) -> DraftVariant) {
        val list = _uiState.value.variants.toMutableList()
        if (index in list.indices) {
            list[index] = transform(list[index])
            _uiState.value = _uiState.value.copy(variants = list, error = null)
        }
    }

    // --- Navigation ---
    fun goBack() {
        val step = _uiState.value.currentStep
        if (step > 0) _uiState.value = _uiState.value.copy(currentStep = step - 1, error = null)
    }

    fun goNext(onComplete: () -> Unit) {
        val state = _uiState.value
        when (state.currentStep) {
            0 -> submitProductInfo()
            1 -> submitOptionTypes()
            2 -> submitOptionValues()
            3 -> submitVariants(onComplete)
        }
    }

    // --- API calls per step ---

    private fun submitProductInfo() {
        val s = _uiState.value
        _uiState.value = s.copy(isSubmitting = true, error = null)
        screenModelScope.launch {
            val result = productRepository.createProduct(
                CreateProductRequest(
                    name = s.name.trim(),
                    description = s.description.trim(),
                    brand = s.brand.trim(),
                    mediaUrls = s.imageUrls,
                )
            )
            when (result) {
                is AsyncResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        createdProductId = result.data.id,
                        currentStep = 1,
                    )
                }
                is AsyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = result.displayMessage ?: "Failed to create product",
                    )
                }
            }
        }
    }

    private fun submitOptionTypes() {
        val s = _uiState.value
        val productId = s.createdProductId ?: return
        if (s.optionTypes.isEmpty()) {
            _uiState.value = s.copy(currentStep = 2)
            return
        }
        _uiState.value = s.copy(isSubmitting = true, error = null)
        screenModelScope.launch {
            val updatedTypes = s.optionTypes.toMutableList()
            for ((i, ot) in s.optionTypes.withIndex()) {
                if (ot.serverId != null) continue
                val result = productRepository.createOptionType(
                    productId, CreateProductOptionTypeRequest(name = ot.name, displayOrder = i + 1)
                )
                when (result) {
                    is AsyncResult.Success -> updatedTypes[i] = ot.copy(serverId = result.data)
                    is AsyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            error = "Failed to create \"${ot.name}\": ${result.displayMessage}",
                        )
                        return@launch
                    }
                }
            }
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                optionTypes = updatedTypes,
                currentStep = 2,
            )
        }
    }

    private fun submitOptionValues() {
        val s = _uiState.value
        val hasAnyValues = s.optionTypes.any { it.values.isNotEmpty() }
        if (!hasAnyValues) {
            _uiState.value = s.copy(currentStep = 3)
            return
        }
        _uiState.value = s.copy(isSubmitting = true, error = null)
        screenModelScope.launch {
            val updatedTypes = s.optionTypes.toMutableList()
            for ((ti, ot) in s.optionTypes.withIndex()) {
                val otId = ot.serverId ?: continue
                val updatedValues = ot.values.toMutableList()
                for ((vi, ov) in ot.values.withIndex()) {
                    if (ov.serverId != null) continue
                    val result = productRepository.createOptionValue(
                        otId, CreateProductOptionValueRequest(value = ov.value, displayOrder = vi + 1)
                    )
                    when (result) {
                        is AsyncResult.Success -> updatedValues[vi] = ov.copy(serverId = result.data)
                        is AsyncResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isSubmitting = false,
                                error = "Failed to create value \"${ov.value}\": ${result.displayMessage}",
                            )
                            return@launch
                        }
                    }
                }
                updatedTypes[ti] = ot.copy(values = updatedValues)
            }
            _uiState.value = _uiState.value.copy(
                isSubmitting = false,
                optionTypes = updatedTypes,
                currentStep = 3,
            )
        }
    }

    private fun submitVariants(onComplete: () -> Unit) {
        val s = _uiState.value
        val productId = s.createdProductId ?: return
        if (s.variants.isEmpty()) {
            _uiState.value = s.copy(completed = true)
            onComplete()
            return
        }
        _uiState.value = s.copy(isSubmitting = true, error = null)
        screenModelScope.launch {
            for (v in s.variants) {
                val result = productRepository.createVariant(
                    productId,
                    CreateProductVariantRequest(
                        sku = v.sku.trim(),
                        price = v.price.toDoubleOrNull() ?: 0.0,
                        stock = v.stock.toIntOrNull() ?: 0,
                        options = v.selectedOptionValueIds,
                    )
                )
                when (result) {
                    is AsyncResult.Success -> {}
                    is AsyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            error = "Failed to create variant \"${v.sku}\": ${result.displayMessage}",
                        )
                        return@launch
                    }
                }
            }
            _uiState.value = _uiState.value.copy(isSubmitting = false, completed = true)
            onComplete()
        }
    }
}
