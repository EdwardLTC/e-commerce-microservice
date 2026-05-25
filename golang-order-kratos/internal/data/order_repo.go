package data

import (
	"bytes"
	"context"

	"golang-order-kratos/gen/avro"
	"golang-order-kratos/gen/ent"
	entorder "golang-order-kratos/gen/ent/order"
	"golang-order-kratos/gen/ent/outboxevent"
	"golang-order-kratos/internal/biz"

	"github.com/google/uuid"
)

type OrderRepo struct {
	client *ent.Client
}

func NewOrderRepo(client *ent.Client) *OrderRepo {
	return &OrderRepo{client: client}
}

func (r *OrderRepo) Create(ctx context.Context, command biz.CreateOrderCommand) (*ent.Order, error) {
	tx, err := r.client.Tx(ctx)
	if err != nil {
		return nil, err
	}

	rolledBack := false
	defer func() {
		if !rolledBack {
			_ = tx.Rollback()
		}
	}()

	orderCreate := tx.Order.Create().
		SetCustomerID(command.CustomerID).
		SetStatus(entorder.StatusCreated).
		SetSubtotal(command.ComputedSubtotal).
		SetTax(command.ComputedTax).
		SetShippingCost(0).
		SetTotal(command.ComputedTotal).
		SetShippingAddress(command.ShippingAddress).
		SetBillingAddress(command.BillingAddress)

	orderEntity, err := orderCreate.Save(ctx)
	if err != nil {
		return nil, err
	}

	itemCreates := make([]*ent.OrderItemCreate, 0, len(command.Items))
	for _, item := range command.Items {
		itemCreates = append(itemCreates, tx.OrderItem.Create().
			SetOrder(orderEntity).
			SetProductID(item.ProductID).
			SetVariantID(item.VariantID).
			SetProductName(item.ProductName).
			SetVariantDescription(item.VariantDescription).
			SetUnitPrice(item.UnitPrice).
			SetQuantity(item.Quantity).
			SetTotalPrice(item.TotalPrice).
			SetImageURL(item.ImageURL))
	}

	if len(itemCreates) > 0 {
		if _, err := tx.OrderItem.CreateBulk(itemCreates...).Save(ctx); err != nil {
			return nil, err
		}
	}

	event := avro.OrderCreatedEvent{
		Order_id:        orderEntity.ID.String(),
		Status:          string(orderEntity.Status),
		Temporary_price: command.ComputedTotal,
		Customer_id:     command.CustomerID.String(),
		Items:           command.EventItems,
	}

	buffer := new(bytes.Buffer)
	if err := event.Serialize(buffer); err != nil {
		return nil, err
	}

	if _, err := tx.OutboxEvent.Create().
		SetAggregateType("order").
		SetAggregateID(orderEntity.ID).
		SetEventType("order.created").
		SetPayload(buffer.Bytes()).
		Save(ctx); err != nil {
		return nil, err
	}

	if err := tx.Commit(); err != nil {
		return nil, err
	}
	rolledBack = true

	return r.Get(ctx, orderEntity.ID)
}

func (r *OrderRepo) Get(ctx context.Context, id uuid.UUID) (*ent.Order, error) {
	return r.client.Order.Query().
		Where(entorder.IDEQ(id)).
		WithItems().
		Only(ctx)
}

func (r *OrderRepo) List(ctx context.Context, query biz.ListOrdersQuery) ([]*ent.Order, int, error) {
	totalBuilder := r.buildListQuery(query)
	totalCount, err := totalBuilder.Count(ctx)
	if err != nil {
		return nil, 0, err
	}

	offset := (query.Page - 1) * query.PageSize
	orders, err := r.buildListQuery(query).
		WithItems().
		Order(ent.Desc(entorder.FieldCreatedAt)).
		Offset(offset).
		Limit(query.PageSize).
		All(ctx)
	if err != nil {
		return nil, 0, err
	}

	return orders, totalCount, nil
}

func (r *OrderRepo) UpdateStatus(ctx context.Context, orderID uuid.UUID, status entorder.Status, errorMessage *string, expectedCurrent entorder.Status) error {
	entity, err := r.client.Order.Get(ctx, orderID)
	if err != nil {
		return err
	}

	if entity.Status == status {
		return nil
	}
	if entity.Status != expectedCurrent {
		return nil
	}

	update := r.client.Order.UpdateOneID(orderID).SetStatus(status)
	if errorMessage != nil {
		update = update.SetErrorMessage(*errorMessage)
	}

	_, err = update.Save(ctx)
	return err
}

func (r *OrderRepo) ListPendingOutbox(ctx context.Context, batch int) ([]*ent.OutboxEvent, error) {
	return r.client.OutboxEvent.Query().
		Where(outboxevent.ProcessedEQ(false)).
		Order(ent.Asc(outboxevent.FieldCreatedAt)).
		Limit(batch).
		All(ctx)
}

func (r *OrderRepo) MarkOutboxProcessed(ctx context.Context, eventID uuid.UUID) error {
	_, err := r.client.OutboxEvent.UpdateOneID(eventID).SetProcessed(true).Save(ctx)
	return err
}

func (r *OrderRepo) buildListQuery(query biz.ListOrdersQuery) *ent.OrderQuery {
	builder := r.client.Order.Query()

	if query.CustomerID != nil {
		builder = builder.Where(entorder.CustomerIDEQ(*query.CustomerID))
	}
	if query.Status != nil {
		builder = builder.Where(entorder.StatusEQ(*query.Status))
	}
	if query.CreatedAfter != nil {
		builder = builder.Where(entorder.CreatedAtGT(*query.CreatedAfter))
	}

	return builder
}
