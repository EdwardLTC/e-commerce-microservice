import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  ArrayMinSize,
  IsArray,
  IsEnum,
  IsDateString,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsPositive,
  IsString,
  IsUUID,
  Min,
  ValidateNested,
} from 'class-validator';

export enum OrderStatus {
  CREATED = 0,
  PENDING_INVENTORY = 1,
  INVENTORY_RESERVED = 2,
  INVENTORY_RESERVED_FAILED = 3,
  PAYMENT_PENDING = 4,
  PAYMENT_COMPLETED = 5,
  PAYMENT_FAILED = 6,
  SHIPPING = 7,
  COMPLETED = 8,
}

export class CreateOrderItemRequestDto {
  @ApiProperty({
    example: '78d28f3b-bf4f-4fe7-81d5-266ca6a4c234',
  })
  @IsUUID()
  variantId: string;

  @ApiProperty({
    example: 2,
  })
  @Type(() => Number)
  @IsNumber()
  @Min(1)
  quantity: number;

  @ApiProperty({
    example: 49.99,
  })
  @Type(() => Number)
  @IsNumber()
  @IsPositive()
  unitPrice: number;
}

export class CreateOrderRequestDto {
  @ApiProperty({
    example: 'b1db89c1-8f78-486c-80db-ddf64b75b8f0',
  })
  @IsUUID()
  customerId: string;

  @ApiProperty({
    type: [CreateOrderItemRequestDto],
  })
  @IsArray()
  @ArrayMinSize(1)
  @ValidateNested({ each: true })
  @Type(() => CreateOrderItemRequestDto)
  items: CreateOrderItemRequestDto[];

  @ApiProperty({
    example: '221B Baker Street, London',
  })
  @IsString()
  @IsNotEmpty()
  shippingAddress: string;

  @ApiProperty({
    example: '221B Baker Street, London',
  })
  @IsString()
  @IsNotEmpty()
  billingAddress: string;
}

export class GetOrdersRequestDto {
  @ApiPropertyOptional({
    example: 1,
  })
  @Type(() => Number)
  @IsOptional()
  @IsNumber()
  @Min(1)
  page?: number;

  @ApiPropertyOptional({
    example: 20,
  })
  @Type(() => Number)
  @IsOptional()
  @IsNumber()
  @Min(1)
  pageSize?: number;

  @ApiPropertyOptional({
    example: 'b1db89c1-8f78-486c-80db-ddf64b75b8f0',
  })
  @IsOptional()
  @IsUUID()
  customerId?: string;

  @ApiPropertyOptional({
    enum: OrderStatus,
    example: OrderStatus.PAYMENT_COMPLETED,
  })
  @Type(() => Number)
  @IsOptional()
  @IsEnum(OrderStatus)
  status?: OrderStatus;

  @ApiPropertyOptional({
    example: '2025-01-01T00:00:00.000Z',
  })
  @IsOptional()
  @IsDateString()
  createdAfter?: string;
}

export class OrderItemResponseDto {
  @ApiProperty()
  id: string;

  @ApiProperty()
  productId: string;

  @ApiProperty()
  variantId: string;

  @ApiProperty()
  productName: string;

  @ApiProperty()
  variantDescription: string;

  @ApiProperty()
  unitPrice: number;

  @ApiProperty()
  salePrice: number;

  @ApiProperty()
  quantity: number;

  @ApiProperty()
  totalPrice: number;

  @ApiProperty()
  imageUrl: string;

  @ApiProperty()
  createdAt: {
    seconds?: number;
    nanos?: number;
  };

  @ApiProperty()
  updatedAt: {
    seconds?: number;
    nanos?: number;
  };
}

export class OrderResponseDto {
  @ApiProperty()
  id: string;

  @ApiProperty()
  customerId: string;

  @ApiProperty({ enum: OrderStatus })
  status: number;

  @ApiProperty()
  subtotal: number;

  @ApiProperty()
  tax: number;

  @ApiProperty()
  shippingCost: number;

  @ApiProperty()
  total: number;

  @ApiProperty()
  shippingAddress: string;

  @ApiProperty()
  billingAddress: string;

  @ApiProperty()
  paymentIntentId: string;

  @ApiProperty()
  createdAt: {
    seconds?: number;
    nanos?: number;
  };

  @ApiProperty()
  updatedAt: {
    seconds?: number;
    nanos?: number;
  };

  @ApiProperty({
    type: [OrderItemResponseDto],
  })
  items: OrderItemResponseDto[];
}

export class CreateOrderResponseDto {
  @ApiProperty()
  id: string;

  @ApiProperty({ enum: OrderStatus })
  status: number;
}

export class OrdersListResponseDto {
  @ApiProperty({
    type: [OrderResponseDto],
  })
  orders: OrderResponseDto[];

  @ApiProperty()
  totalCount: number;

  @ApiProperty()
  currentPage: number;

  @ApiProperty()
  totalPages: number;
}
