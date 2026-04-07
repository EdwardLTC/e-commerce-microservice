package org.edward.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    val variantId: String,
    val productId: String,
    val productName: String,
    val variantDescription: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String?
) {
    val totalPrice: Double get() = price * quantity
}

class CartRepository {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items

    val totalAmount: Double get() = _items.value.sumOf { it.totalPrice }
    val itemCount: Int get() = _items.value.sumOf { it.quantity }

    fun addItem(item: CartItem) {
        _items.update { current ->
            val existing = current.find { it.variantId == item.variantId }
            if (existing != null) {
                current.map {
                    if (it.variantId == item.variantId) it.copy(quantity = it.quantity + item.quantity)
                    else it
                }
            } else {
                current + item
            }
        }
    }

    fun removeItem(variantId: String) {
        _items.update { current -> current.filter { it.variantId != variantId } }
    }

    fun updateQuantity(variantId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(variantId)
            return
        }
        _items.update { current ->
            current.map {
                if (it.variantId == variantId) it.copy(quantity = quantity) else it
            }
        }
    }

    fun clear() {
        _items.value = emptyList()
    }
}
