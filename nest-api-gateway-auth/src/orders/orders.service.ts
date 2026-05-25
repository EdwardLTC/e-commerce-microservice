import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { lastValueFrom } from 'rxjs';
import { com } from '../generated/.proto/Order';
import { CreateOrderRequestDto, GetOrdersRequestDto } from './orders.model';
import OrderService = com.ecommerce.golang.order.OrderService;

@Injectable()
export class OrdersService {
  private readonly orderClient = this.client.getService<OrderService>('OrderService');

  constructor(@Inject('com.ecommerce.golang.order') private client: ClientGrpc) {}

  public async createOrder(data: CreateOrderRequestDto) {
    return lastValueFrom(
      this.orderClient.createOrder({
        customerId: data.customerId,
        shippingAddress: data.shippingAddress,
        billingAddress: data.billingAddress,
        items: data.items.map(item => ({
          variantId: item.variantId,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
        })),
      }),
    );
  }

  public async getOrder(id: string) {
    return lastValueFrom(this.orderClient.getOrder({ id }));
  }

  public async getOrders(query: GetOrdersRequestDto) {
    return lastValueFrom(
      this.orderClient.getOrders({
        page: query.page ?? 1,
        pageSize: query.pageSize ?? 20,
        customerId: query.customerId,
        createdAfter: query.createdAfter
          ? {
              seconds: Math.floor(new Date(query.createdAfter).getTime() / 1000),
              nanos: 0,
            }
          : undefined,
      }),
    );
  }
}
