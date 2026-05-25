package com.ecommerce.springboot.product.repositories

import com.ecommerce.springboot.product.database.KafkaOutboxTable
import com.ecommerce.springboot.product.database.KafkaOutboxTable.OutboxStatus
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.Base64

@Repository
@Transactional
class KafkaOutboxRepository {
    companion object {
        const val MAX_ATTEMPTS = 5
    }

    data class OutboxMessage(
        val id: UUID,
        val topic: String,
        val messageKey: String?,
        val payload: ByteArray,
        val attempts: Int,
    )

    fun enqueue(topic: String, payload: ByteArray, messageKey: String? = null): UUID {
        return transaction {
            KafkaOutboxTable.insertAndGetId { row ->
                row[KafkaOutboxTable.topic] = topic
                row[KafkaOutboxTable.messageKey] = messageKey
                row[KafkaOutboxTable.payloadBase64] = Base64.getEncoder().encodeToString(payload)
                row[KafkaOutboxTable.status] = OutboxStatus.PENDING
                row[KafkaOutboxTable.attempts] = 0
                row[KafkaOutboxTable.errorMessage] = null
            }.value
        }
    }

    fun claimNextBatch(batchSize: Int): List<OutboxMessage> {
        if (batchSize <= 0) return emptyList()

        return transaction {
            val rows = KafkaOutboxTable.selectAll()
                .where { KafkaOutboxTable.status eq OutboxStatus.PENDING }
                .orderBy(KafkaOutboxTable.createdAt to SortOrder.ASC)
                .limit(batchSize)
                .forUpdate()
                .toList()

            rows.forEach { row ->
                KafkaOutboxTable.update({ KafkaOutboxTable.id eq row[KafkaOutboxTable.id].value }) {
                    it[status] = OutboxStatus.PROCESSING
                    it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                    it[errorMessage] = null
                }
            }

            rows.map { row ->
                OutboxMessage(
                    id = row[KafkaOutboxTable.id].value,
                    topic = row[KafkaOutboxTable.topic],
                    messageKey = row[KafkaOutboxTable.messageKey],
                    payload = Base64.getDecoder().decode(row[KafkaOutboxTable.payloadBase64]),
                    attempts = row[KafkaOutboxTable.attempts],
                )
            }
        }
    }

    fun requeueStaleProcessing(staleAfterMinutes: Long = 10): Int {
        val staleBefore = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(staleAfterMinutes)

        return transaction {
            KafkaOutboxTable.update({
                (KafkaOutboxTable.status eq OutboxStatus.PROCESSING) and
                    (KafkaOutboxTable.updatedAt less staleBefore)
            }) {
                it[status] = OutboxStatus.PENDING
                it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[errorMessage] = null
            }
        }
    }

    fun markPublished(id: UUID): Int {
        return transaction {
            KafkaOutboxTable.update({ KafkaOutboxTable.id eq id }) {
                it[status] = OutboxStatus.PUBLISHED
                it[publishedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[errorMessage] = null
            }
        }
    }

    fun markRetry(id: UUID, errorMessage: String): Int {
        return transaction {
            val row = KafkaOutboxTable.selectAll()
                .where { KafkaOutboxTable.id eq id }
                .singleOrNull()
                ?: return@transaction 0

            val nextAttempts = row[KafkaOutboxTable.attempts] + 1
            val nextStatus = if (nextAttempts >= MAX_ATTEMPTS) OutboxStatus.FAILED else OutboxStatus.PENDING

            KafkaOutboxTable.update({ KafkaOutboxTable.id eq id }) {
                it[attempts] = nextAttempts
                it[status] = nextStatus
                it[updatedAt] = OffsetDateTime.now(ZoneOffset.UTC)
                it[KafkaOutboxTable.errorMessage] = errorMessage.take(2000)
            }
        }
    }
}

