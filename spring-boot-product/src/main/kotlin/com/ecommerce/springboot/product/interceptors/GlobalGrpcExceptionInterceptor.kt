package com.ecommerce.springboot.product.interceptors

import io.grpc.*
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.slf4j.LoggerFactory

@GrpcGlobalServerInterceptor
class GlobalGrpcExceptionInterceptor : ServerInterceptor {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val listener = next.startCall(call, headers)
        return ExceptionHandlingListener(listener, call, headers)
    }

    private inner class ExceptionHandlingListener<ReqT, RespT>(
        delegate: ServerCall.Listener<ReqT>,
        private val call: ServerCall<ReqT, RespT>,
        private val headers: Metadata
    ) : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {

        private var requestBody: ReqT? = null

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

                if (status.code == Status.INTERNAL.code) {
                    val logBuilder = StringBuilder()
                    logBuilder.appendLine("⚠️ gRPC Exception Interceptor")
                    logBuilder.appendLine("Exception  : ${e::class.qualifiedName}")
                    logBuilder.appendLine("Message    : ${e.message}")
                    logBuilder.appendLine("Metadata   : ")
                    headers.keys().forEach {
                        val key = Metadata.Key.of(it, Metadata.ASCII_STRING_MARSHALLER)
                        logBuilder.appendLine("  - $it: ${headers[key]}")
                    }
                    logBuilder.appendLine("Request    : $requestBody")

                    logger.error(logBuilder.toString(), e)
                }

                call.close(status, Metadata())
            }
        }
    }
}