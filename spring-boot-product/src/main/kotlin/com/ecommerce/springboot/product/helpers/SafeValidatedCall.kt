package com.ecommerce.springboot.product.helpers

import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.slf4j.LoggerFactory

suspend fun <Req, DTO : Any, Res> safeValidatedCall(
    request: Req,
    transform: FromGrpcRequest<Req, DTO>,
    block: suspend (DTO) -> Res
): Res {
    try {
        val dto = transform.from(request)
        ValidationHelper.validate<DTO>(dto)
        return block(dto)
    } catch (e: Exception) {
        var status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT
            is StatusRuntimeException -> e.status
            else -> Status.INTERNAL
        }.withDescription(e.message).withCause(e)

        if (status.code == Status.INTERNAL.code || status.code == Status.UNKNOWN.code || status.code == Status.UNAVAILABLE.code) {
            val logBuilder = StringBuilder()
            logBuilder.appendLine("⚠️ gRPC Exception Interceptor")
            logBuilder.appendLine("Exception  : ${e::class.qualifiedName}")
            logBuilder.appendLine("Message    : ${e.message}")
            logBuilder.appendLine("Request    : $request")
            LoggerFactory.getLogger("gRPC Exception Interceptor").error(logBuilder.toString(), e)
            status = Status.INTERNAL.withDescription("An unexpected error occurred. Please try again later.")
        }

        throw status.asRuntimeException()
    }
}