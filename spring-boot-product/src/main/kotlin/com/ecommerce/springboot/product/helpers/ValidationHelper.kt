package com.ecommerce.springboot.product.helpers

import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
object ValidationHelper {
    lateinit var validator: Validator

    @Autowired
    fun init(validator: Validator) {
        ValidationHelper.validator = validator
    }

    fun <T : Any> validate(dto: Any) {
        val violations = validator.validate(dto)
        if (violations.isNotEmpty()) {
            val message = violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
            throw IllegalArgumentException(("Invalid request: $message"))
        }
    }
}