package com.ecommerce.springboot.product.clients

import com.ecommerce.aspnet.user.User
import com.ecommerce.aspnet.user.UserServiceGrpc
import com.ecommerce.springboot.product.configs.GrpcClientConfig
import com.google.protobuf.StringValue
import io.grpc.ManagedChannelBuilder
import org.springframework.stereotype.Service

@Service
class UserServiceClient(grpcClientConfig: GrpcClientConfig) {

    private val client = UserServiceGrpc.newBlockingV2Stub(
        ManagedChannelBuilder.forAddress(grpcClientConfig.user.host, grpcClientConfig.user.port)
            .usePlaintext()
            .build()
    )

    fun get(userId: String): User.UserProfile {
        return client.getUserById(User.GetUserByIdRequest.newBuilder().setId(StringValue.of(userId)).build())
    }
}