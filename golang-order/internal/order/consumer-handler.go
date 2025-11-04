package order

import (
	"bytes"
	"context"
	"fmt"
	"golang-order/gen/avro"
	"golang-order/gen/ent"
	"golang-order/gen/ent/order"
	"log"

	"github.com/confluentinc/confluent-kafka-go/kafka"
	"github.com/google/uuid"
)

type ConsumerHandler struct {
	db *ent.Client
}

func NewConsumerHandler(db *ent.Client) *ConsumerHandler {
	return &ConsumerHandler{
		db: db,
	}
}

func (c ConsumerHandler) HandleMessage(msg *kafka.Message, ack *kafka.Consumer) {
	topic := *msg.TopicPartition.Topic

	switch topic {
	case "stock.reduction.fail":
		event, err := avro.DeserializeStockReductionFailedEvent(bytes.NewReader(msg.Value))
		if err != nil {
			log.Printf("failed to deserialize StockReductionFailedEvent: %v", err)
			return
		}
		c.onStockEvent(false, uuid.MustParse(event.Order_id), event.Message, msg, ack)

	case "stock.reduction.success":
		event, err := avro.DeserializeStockReductionSuccessEvent(bytes.NewReader(msg.Value))
		if err != nil {
			log.Printf("failed to deserialize StockReductionSuccessEvent: %v", err)
			return
		}
		c.onStockEvent(true, uuid.MustParse(event.Order_id), "", msg, ack)
	case "payment.fail":
		event, err := avro.DeserializePaymentFailedEvent(bytes.NewReader(msg.Value))
		if err != nil {
			log.Printf("failed to deserialize PaymentFailedEvent: %v", err)
			return
		}
		c.onPaymentEvent(false, uuid.MustParse(event.Order_id), event.Fail_reason, msg, ack)
	case "payment.success":
		event, err := avro.DeserializePaymentSuccessEvent(bytes.NewReader(msg.Value))
		if err != nil {
			log.Printf("failed to deserialize PaymentSuccessEvent: %v", err)
			return
		}
		c.onPaymentEvent(true, uuid.MustParse(event.Order_id), "", msg, ack)
	default:
		log.Printf("unknown topic: %s", topic)
	}
}

func (c ConsumerHandler) onStockEvent(isSuccess bool, orderID uuid.UUID, errMessage string, msg *kafka.Message, ack *kafka.Consumer) {
	builder := c.db.Order.UpdateOneID(orderID).Where(order.StatusEQ(order.StatusCreated))

	if isSuccess {
		builder = builder.
			SetStatus(order.StatusInventoryReserved)
	} else {
		builder = builder.
			SetStatus(order.StatusInventoryReservedFailed).
			SetErrorMessage(errMessage)
	}

	_, err := builder.Save(context.Background())

	if err != nil {
		fmt.Printf(err.Error())
		return
	}

	// should put into transaction outbox
	_, err = ack.CommitMessage(msg)
	if err != nil {
		fmt.Printf("failed to commit message: %v", err)
		return
	}
}

func (c ConsumerHandler) onPaymentEvent(isSuccess bool, orderID uuid.UUID, errMessage string, msg *kafka.Message, ack *kafka.Consumer) {
	builder := c.db.Order.UpdateOneID(orderID).Where(order.StatusEQ(order.StatusInventoryReserved))

	if isSuccess {
		builder = builder.
			SetStatus(order.StatusPaymentCompleted)
	} else {
		builder = builder.
			SetStatus(order.StatusPaymentFailed).
			SetErrorMessage(errMessage)
	}

	_, err := builder.Save(context.Background())

	if err != nil {
		fmt.Printf(err.Error())
		return
	}

	// should put into transaction outbox
	_, err = ack.CommitMessage(msg)
	if err != nil {
		fmt.Printf("failed to commit message: %v", err)
		return
	}
}
