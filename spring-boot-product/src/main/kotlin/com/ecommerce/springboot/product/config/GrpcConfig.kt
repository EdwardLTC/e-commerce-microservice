package com.ecommerce.springboot.product.config

import com.ecommerce.springboot.product.interceptors.ValidationInterceptor
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptor
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfig(
) {

//    @Bean
//    fun validationInterceptor(): ServerInterceptor = ValidationInterceptor()
//
//
//    @Bean
//    fun globalGrpcConfigurer(): GrpcServerConfigurer {
//        return GrpcServerConfigurer { serverBuilder: ServerBuilder<*> ->
//            serverBuilder.intercept(validationInterceptor)
//        }
//    }
}