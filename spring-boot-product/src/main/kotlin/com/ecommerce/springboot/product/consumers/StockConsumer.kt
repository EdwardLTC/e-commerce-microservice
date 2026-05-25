package com.ecommerce.springboot.product.consumers

import com.ecommerce.springboot.product.dto.ReserveStock
import com.ecommerce.springboot.product.repositories.KafkaOutboxRepository
import com.ecommerce.springboot.product.repositories.StockRepository
import com.ecommerce.springboot.product.utils.AvroUtils
import com.example.order.OrderCreatedEvent
import com.example.payment.PaymentFailedEvent
import com.example.stock.StockReductionFailedEvent
import com.example.stock.StockReductionSuccessEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.util.*

@Service
class StockConsumer(
    private val stockRepository: StockRepository,
    private val kafkaOutboxRepository: KafkaOutboxRepository,
) {

    companion object {
        val TOPIC_ORDER_CREATED = "order.created"
        val TOPIC_PAYMENT_FAILED = "payment.fail"
    }

    @KafkaListener(topics = ["order.created"], groupId = "stock-consumer-group")
    fun onOrderCreated(payload: ByteArray, ack: Acknowledgment) {
        val event = AvroUtils.deserialize(payload, OrderCreatedEvent.getClassSchema(), OrderCreatedEvent::class.java)
        val stockReductionSuccessEvent = StockReductionSuccessEvent.newBuilder()
            .setOrderId(event.orderId)
            .setPrice(event.temporaryPrice)
            .build()

        try {
            stockRepository.reserveStock(event.items.map {
                ReserveStock(
                    variantId = UUID.fromString(it.variantId),
                    quantity = it.quantity,
                    unitPrice = it.unitPrice
                )
            }, event.orderId, AvroUtils.serialize(stockReductionSuccessEvent))
        } catch (e: IllegalArgumentException) {
            val stockReductionFailedEvent = StockReductionFailedEvent.newBuilder()
                .setOrderId(event.orderId)
                .setMessage(e.message ?: "Unknown error")
                .build()

            kafkaOutboxRepository.enqueue(
                topic = "stock.reduction.fail",
                payload = AvroUtils.serialize(stockReductionFailedEvent),
                messageKey = event.orderId,
            )
        } catch (e: Exception) {
            throw e
        }
        ack.acknowledge()
    }

    @KafkaListener(topics = ["payment.fail"], groupId = "stock-consumer-group")
    fun onStockReductionRollback(payload: ByteArray, ack: Acknowledgment) {
        val event = AvroUtils.deserialize(payload, PaymentFailedEvent.getClassSchema(), PaymentFailedEvent::class.java)

        stockRepository.releaseStock(event.orderId)

        ack.acknowledge()
    }
}
