package com.ecommerce.springboot.product.interceptors

import io.grpc.*
import java.lang.reflect.Method

class ValidationInterceptor : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val listener = next.startCall(call, headers)

        return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            override fun onMessage(message: ReqT) {
                message?.let { it ->
                    try {
                        val validateMethod: Method? = it::class.java.methods.find {
                            it.name == "validate" && it.parameterCount == 0
                        }
                        val error = validateMethod?.invoke(message) as? String
                        if (!error.isNullOrBlank()) {
                            call.close(Status.INVALID_ARGUMENT.withDescription(error), headers)
                            return
                        }
                    } catch (e: Exception) {
                        call.close(Status.INTERNAL.withDescription("Validation failed: ${e.message}"), headers)
                        return
                    }
                }

                super.onMessage(message)
            }
        }
    }
}