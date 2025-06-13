package com.ecommerce.springboot.product.clients

import com.ecommerce.aspnet.user.User
import com.ecommerce.aspnet.user.UserServiceGrpc
import com.google.protobuf.StringValue
import io.grpc.ManagedChannelBuilder
import org.springframework.stereotype.Service


@Service
class UserServiceClient {
    private val client = UserServiceGrpc.newBlockingV2Stub(
        ManagedChannelBuilder.forAddress("localhost", 5232)
            .usePlaintext()
            .build()
    )

    fun get(userId: String): User.UserProfile {

        return client.getUserById(User.GetUserByIdRequest.newBuilder().setId(StringValue.of(userId)).build())
    }
}