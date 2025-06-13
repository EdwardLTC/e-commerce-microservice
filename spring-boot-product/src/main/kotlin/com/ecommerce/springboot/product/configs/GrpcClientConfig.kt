package com.ecommerce.springboot.product.configs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "grpc.services")
class GrpcClientConfig {
    lateinit var user: GrpcServiceProperties

    class GrpcServiceProperties {
        lateinit var host: String
        var port: Int = 0
    }
}