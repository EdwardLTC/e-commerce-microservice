package com.ecommerce.springboot.product.services

import com.ecommerce.springboot.product.dto.ReserveStock
import com.ecommerce.springboot.product.repositories.StockRepository
import com.ecommerce.springboot.product.utils.AvroUtils
import com.example.order.OrderCreatedEvent
import com.example.stock.StockReductionFailedEvent
import com.example.stock.StockReductionSuccessEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.util.*

@Service
class StockConsumer(
    private val stockRepository: StockRepository,
    private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
) {

    @KafkaListener(topics = ["order.created"], groupId = "stock-consumer-group")
    fun onOrderCreated(payload: ByteArray, ack: Acknowledgment) {
        val event = AvroUtils.deserialize(payload, OrderCreatedEvent.getClassSchema(), OrderCreatedEvent::class.java)
        try {
            stockRepository.reserveStock(event.items.map {
                ReserveStock(
                    variantId = UUID.fromString(it.variantId),
                    quantity = it.quantity,
                    unitPrice = it.unitPrice
                )
            }, event.orderId)

            val stockReduceSuccessEvent = StockReductionSuccessEvent.newBuilder()
                .setOrderId(event.orderId)
                .setPrice(event.temporaryPrice)
                .build()

            kafkaTemplate.send("stock.reduce.success", AvroUtils.serialize(stockReduceSuccessEvent))
            ack.acknowledge()
        } catch (e: IllegalArgumentException) {
            val stockReductionFailedEvent = StockReductionFailedEvent.newBuilder()
                .setOrderId(event.orderId)
                .setMessage(e.message ?: "Unknown error")
                .build()

            kafkaTemplate.send("stock.reduce.fail", AvroUtils.serialize(stockReductionFailedEvent))
            ack.acknowledge()
        } catch (e: Exception) {
            throw e
        }

    }
}