package org.edward.app.presentations.screens.main.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.edward.app.data.remote.product.Product
import org.edward.app.data.remote.product.ProductRepository
import org.edward.app.data.utils.AsyncResult
import org.koin.core.component.KoinComponent

class HomeScreenModel(private val productRepository: ProductRepository) :
    ScreenModel, KoinComponent {

    companion object {
        data class UiState(
            val loading: Boolean = true,
            val products: List<Product> = emptyList(),
            val error: String? = null,
            val isRefreshing: Boolean = false
        )
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadProducts()
    }

    fun loadProducts() {
        screenModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)

            when (val result = productRepository.getProducts()) {
                is AsyncResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        products = result.data
                    )
                }

                is AsyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = result.exception?.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun onPullToRefreshTrigger() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        screenModelScope.launch {
            loadProducts()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}