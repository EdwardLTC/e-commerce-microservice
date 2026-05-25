package biz

import (
	"golang-order-kratos/gen/ent"
	entorder "golang-order-kratos/gen/ent/order"
	pb "golang-order-kratos/gen/proto"

	"google.golang.org/protobuf/types/known/timestamppb"
)

func ToProtoOrder(entity *ent.Order) *pb.Order {
	items := make([]*pb.OrderItem, 0, len(entity.Edges.Items))
	for _, item := range entity.Edges.Items {
		items = append(items, &pb.OrderItem{
			Id:                 item.ID.String(),
			ProductId:          item.ProductID.String(),
			VariantId:          item.VariantID.String(),
			ProductName:        item.ProductName,
			VariantDescription: item.VariantDescription,
			UnitPrice:          item.UnitPrice,
			SalePrice:          item.UnitPrice,
			Quantity:           int32(item.Quantity),
			TotalPrice:         item.TotalPrice,
			ImageUrl:           item.ImageURL,
			CreatedAt:          timestamppb.New(item.CreatedAt),
			UpdatedAt:          timestamppb.New(item.UpdatedAt),
		})
	}

	return &pb.Order{
		Id:              entity.ID.String(),
		CustomerId:      entity.CustomerID.String(),
		Status:          toProtoOrderStatus(entity.Status),
		Subtotal:        entity.Subtotal,
		Tax:             entity.Tax,
		ShippingCost:    entity.ShippingCost,
		Total:           entity.Total,
		ShippingAddress: entity.ShippingAddress,
		BillingAddress:  entity.BillingAddress,
		CreatedAt:       timestamppb.New(entity.CreatedAt),
		UpdatedAt:       timestamppb.New(entity.UpdatedAt),
		Items:           items,
	}
}

func toProtoOrderStatus(status entorder.Status) pb.OrderStatus {
	switch status {
	case entorder.StatusCreated:
		return pb.OrderStatus_CREATED
	case entorder.StatusPendingInventory:
		return pb.OrderStatus_PENDING_INVENTORY
	case entorder.StatusInventoryReserved:
		return pb.OrderStatus_INVENTORY_RESERVED
	case entorder.StatusInventoryReservedFailed:
		return pb.OrderStatus_INVENTORY_RESERVED_FAILED
	case entorder.StatusPaymentPending:
		return pb.OrderStatus_PAYMENT_PENDING
	case entorder.StatusPaymentCompleted:
		return pb.OrderStatus_PAYMENT_COMPLETED
	case entorder.StatusPaymentFailed:
		return pb.OrderStatus_PAYMENT_FAILED
	case entorder.StatusShipping:
		return pb.OrderStatus_SHIPPING
	case entorder.StatusCompleted:
		return pb.OrderStatus_COMPLETED
	default:
		return pb.OrderStatus_CREATED
	}
}

func fromProtoOrderStatus(status pb.OrderStatus) entorder.Status {
	switch status {
	case pb.OrderStatus_PENDING_INVENTORY:
		return entorder.StatusPendingInventory
	case pb.OrderStatus_INVENTORY_RESERVED:
		return entorder.StatusInventoryReserved
	case pb.OrderStatus_INVENTORY_RESERVED_FAILED:
		return entorder.StatusInventoryReservedFailed
	case pb.OrderStatus_PAYMENT_PENDING:
		return entorder.StatusPaymentPending
	case pb.OrderStatus_PAYMENT_COMPLETED:
		return entorder.StatusPaymentCompleted
	case pb.OrderStatus_PAYMENT_FAILED:
		return entorder.StatusPaymentFailed
	case pb.OrderStatus_SHIPPING:
		return entorder.StatusShipping
	case pb.OrderStatus_COMPLETED:
		return entorder.StatusCompleted
	default:
		return entorder.StatusCreated
	}
}
