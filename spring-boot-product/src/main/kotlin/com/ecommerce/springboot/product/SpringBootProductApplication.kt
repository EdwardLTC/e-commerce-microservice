package com.ecommerce.springboot.product

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
class SpringBootProductApplication

fun main(args: Array<String>) {
    runApplication<SpringBootProductApplication>(*args)
}
