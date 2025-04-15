package com.ecommerce.springboot.post

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
class SpringBootPostApplication

fun main(args: Array<String>) {
    runApplication<SpringBootPostApplication>(*args)
}
