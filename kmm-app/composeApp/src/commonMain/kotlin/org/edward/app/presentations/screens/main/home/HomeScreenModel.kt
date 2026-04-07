package org.edward.app.presentations.screens.main.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.edward.app.data.remote.product.Product
import org.edward.app.data.remote.product.ProductRepository
import org.edward.app.data.utils.AsyncResult

class HomeScreenModel(private val productRepository: ProductRepository) : ScreenModel {

    companion object {
        private const val PAGE_SIZE = 10
    }

    data class UiState(
        val loading: Boolean = true,
        val products: List<Product> = emptyList(),
        val error: String? = null,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    var lastViewedPage: Int = 0
        private set

    fun saveCurrentPage(page: Int) {
        lastViewedPage = page
    }

    init {
        loadProducts()
    }

    fun loadProducts() {
        screenModelScope.launch {
            _uiState.value = UiState(loading = true)
            when (val result = productRepository.getProducts(skip = 0, take = PAGE_SIZE)) {
                is AsyncResult.Success -> {
                    _uiState.value = UiState(
                        loading = false,
                        products = result.data,
                        hasMore = result.data.size >= PAGE_SIZE,
                    )
                }
                is AsyncResult.Error -> {
                    _uiState.value = UiState(
                        loading = false,
                        error = result.displayMessage ?: "Failed to load products",
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        _uiState.value = state.copy(isLoadingMore = true)

        screenModelScope.launch {
            when (val result = productRepository.getProducts(skip = state.products.size, take = PAGE_SIZE)) {
                is AsyncResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        products = _uiState.value.products + result.data,
                        hasMore = result.data.size >= PAGE_SIZE,
                    )
                }
                is AsyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                    )
                }
            }
        }
    }
}
