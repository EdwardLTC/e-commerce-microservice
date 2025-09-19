package org.edward.app.data.remote.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val address: Address,
    val email: String,
    val id: Int,
    val name: Name,
    val password: String,
    val phone: String,
    val username: String
)

@Serializable
data class Address(
    val city: String,
    val geolocation: Geolocation,
    val number: Int,
    val street: String,
    val zipcode: String
)

@Serializable
data class Geolocation(
    val lat: String, val long: String
)

@Serializable
data class Name(
    val firstname: String, val lastname: String
)