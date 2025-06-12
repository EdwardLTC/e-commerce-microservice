import { Body, Controller, Get, HttpCode, HttpStatus, ParseUUIDPipe, Post, Query, Req } from '@nestjs/common';
import { ProductsService } from './products.service';
import { ApiBearerAuth } from '@nestjs/swagger';
import { CreateProductRequest, GetProductsRequest, GetProductsResponse } from './products.model';
import { RequestWithToken } from '../auth/auth.model';

@Controller('products')
@ApiBearerAuth()
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @Get()
  @HttpCode(HttpStatus.OK)
  public async getProducts(@Query() data: GetProductsRequest): Promise<GetProductsResponse[]> {
    return await this.productsService.getProducts(data);
  }

  @Get(':id')
  @HttpCode(HttpStatus.OK)
  public async getProductById(@Query('id', new ParseUUIDPipe()) id: string): Promise<GetProductsResponse> {
    return await this.productsService.getProductById(id);
  }

  @Post()
  @HttpCode(HttpStatus.CREATED)
  public async createProduct(@Body() data: CreateProductRequest, @Req() req: RequestWithToken): Promise<GetProductsResponse> {
    return await this.productsService.createProduct(data, req.token.id);
  }
}
