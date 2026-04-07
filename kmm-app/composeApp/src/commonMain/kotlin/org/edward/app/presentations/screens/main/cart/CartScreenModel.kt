package org.edward.app.presentations.screens.main.cart

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.StateFlow
import org.edward.app.data.local.CartItem
import org.edward.app.data.local.CartRepository

class CartScreenModel(
    private val cartRepository: CartRepository,
) : ScreenModel {

    val items: StateFlow<List<CartItem>> = cartRepository.items

    val totalAmount: Double get() = cartRepository.totalAmount

    fun updateQuantity(variantId: String, quantity: Int) {
        cartRepository.updateQuantity(variantId, quantity)
    }

    fun removeItem(variantId: String) {
        cartRepository.removeItem(variantId)
    }

    fun clearCart() {
        cartRepository.clear()
    }
}
