package com.ecommerce.springboot.product

import org.jetbrains.exposed.v1.spring.boot.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableTransactionManagement
@SpringBootApplication
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
class SpringBootProductApplication

fun main(args: Array<String>) {
    runApplication<SpringBootProductApplication>(*args)
}
