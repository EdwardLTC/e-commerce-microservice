package com.ecommerce.springboot.product.interceptors

import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@GrpcGlobalServerInterceptor
class GlobalGrpcExceptionInterceptor : ServerInterceptor {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val listener = next.startCall(call, headers)

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            var requestBody: ReqT? = null
            override fun onMessage(message: ReqT) {
                requestBody = message
                super.onMessage(message)
            }

            override fun onHalfClose() {
                try {
                    super.onHalfClose()
                } catch (e: Exception) {
                    val status = when (e) {
                        is IllegalArgumentException -> Status.INVALID_ARGUMENT
                        else -> Status.INTERNAL
                    }.withDescription(e.message).withCause(e)

                    if (status == Status.INTERNAL) {
                        val logBuilder = StringBuilder()
                        logBuilder.appendLine("⚠️ gRPC Exception Interceptor")
                        logBuilder.appendLine("Exception  : ${e::class.qualifiedName}")
                        logBuilder.appendLine("Message    : ${e.message}")
                        logBuilder.appendLine("Metadata   : ")
                        headers.keys().forEach { it ->
                            val key = Metadata.Key.of(it, Metadata.ASCII_STRING_MARSHALLER)
                            logBuilder.appendLine("  - $it: ${headers[key]}")
                        }
                        logBuilder.appendLine("Request    : ${requestBody.toString()}")

                        logger.error(logBuilder.toString(), e)
                    }

                    call.close(status, Metadata())
                }
            }
        }
    }

}