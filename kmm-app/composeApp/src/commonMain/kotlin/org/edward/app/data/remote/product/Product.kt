package org.edward.app.data.remote.product

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val category: String,
    val description: String,
    val id: Int,
    val image: String,
    val price: String,
    val title: String
)