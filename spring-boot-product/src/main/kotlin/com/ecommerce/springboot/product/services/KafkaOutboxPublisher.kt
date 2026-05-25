package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.repositories.KafkaOutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class KafkaOutboxPublisher(
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
    private val outboxRepository: KafkaOutboxRepository,
) {
    private val logger = LoggerFactory.getLogger(KafkaOutboxPublisher::class.java)

    @Scheduled(fixedDelayString = "\${app.kafka-outbox.poll-delay-ms:5000}")
    fun publishPendingMessages() {
        outboxRepository.requeueStaleProcessing()

        val messages = outboxRepository.claimNextBatch(BATCH_SIZE)
        if (messages.isEmpty()) {
            return
        }

        messages.forEach { message ->
            try {
                val future = message.messageKey?.let { key ->
                    kafkaTemplate.send(message.topic, key, message.payload)
                } ?: kafkaTemplate.send(message.topic, message.payload)

                future.get(10, TimeUnit.SECONDS)
                outboxRepository.markPublished(message.id)
            } catch (e: Exception) {
                logger.warn("Failed to publish outbox message {} to {}", message.id, message.topic, e)
                outboxRepository.markRetry(message.id, e.message ?: e::class.java.simpleName)
            }
        }
    }

    companion object {
        private const val BATCH_SIZE = 100
    }
}

