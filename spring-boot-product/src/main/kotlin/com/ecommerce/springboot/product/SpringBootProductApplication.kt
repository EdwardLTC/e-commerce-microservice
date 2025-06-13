package com.ecommerce.springboot.product

import com.ecommerce.springboot.product.configs.GrpcClientConfig
import org.jetbrains.exposed.v1.spring.boot.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@EnableTransactionManagement
@SpringBootApplication
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
@EnableConfigurationProperties(GrpcClientConfig::class)
class SpringBootProductApplication

fun main(args: Array<String>) {
    runApplication<SpringBootProductApplication>(*args)
}
