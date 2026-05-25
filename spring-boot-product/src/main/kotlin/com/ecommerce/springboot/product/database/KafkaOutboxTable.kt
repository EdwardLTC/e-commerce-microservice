package com.ecommerce.springboot.product.database

import com.ecommerce.springboot.product.database.utilities.PGEnum
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object KafkaOutboxTable : UUIDTable("kafka_outbox") {
    enum class OutboxStatus {
        PENDING,
        PROCESSING,
        PUBLISHED,
        FAILED,
    }

    val topic = varchar("topic", 255)
    val messageKey = varchar("message_key", 255).nullable()
    val payloadBase64 = text("payload_base64")
    val status = customEnumeration(
        name = "status",
        sql = "kafka_outbox_status",
        fromDb = { value -> OutboxStatus.valueOf(value as String) },
        toDb = { PGEnum("kafka_outbox_status", it) }
    ).default(OutboxStatus.PENDING)
    val attempts = integer("attempts").default(0)
    val errorMessage = text("error_message").nullable()
    val publishedAt = timestampWithTimeZone("published_at").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
}

