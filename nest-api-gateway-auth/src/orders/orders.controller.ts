import { Body, Controller, Get, HttpCode, HttpStatus, Param, ParseUUIDPipe, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiCreatedResponse, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CreateOrderRequestDto, CreateOrderResponseDto, GetOrdersRequestDto, OrderResponseDto, OrdersListResponseDto } from './orders.model';
import { OrdersService } from './orders.service';

@Controller('orders')
@ApiTags('Orders')
@ApiBearerAuth()
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  @Post()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create an order' })
  @ApiCreatedResponse({ type: CreateOrderResponseDto })
  public async createOrder(@Body() body: CreateOrderRequestDto) {
    return this.ordersService.createOrder(body);
  }

  @Get(':id')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Get an order by id' })
  @ApiOkResponse({ type: OrderResponseDto })
  public async getOrder(@Param('id', new ParseUUIDPipe()) id: string) {
    return this.ordersService.getOrder(id);
  }

  @Get()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'List orders' })
  @ApiOkResponse({ type: OrdersListResponseDto })
  public async getOrders(@Query() query: GetOrdersRequestDto) {
    return this.ordersService.getOrders(query);
  }
}
