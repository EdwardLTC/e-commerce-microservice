import { Body, Controller, Get, HttpCode, HttpStatus, ParseUUIDPipe, Post, Query, Req } from '@nestjs/common';
import { ProductsService } from './products.service';
import { ApiBearerAuth } from '@nestjs/swagger';
import {
  CreateProductOptionTypeRequest,
  CreateProductOptionValueRequest,
  CreateProductRequest,
  CreateProductVariantRequest,
  GetProductsRequest,
} from './products.model';
import { RequestWithToken } from '../auth/auth.model';

@Controller('products')
@ApiBearerAuth()
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @Get()
  @HttpCode(HttpStatus.OK)
  public async getProducts(@Query() data: GetProductsRequest) {
    return this.productsService.getProducts(data);
  }

  @Get(':id')
  @HttpCode(HttpStatus.OK)
  public async getProductById(@Query('id', new ParseUUIDPipe()) id: string) {
    return this.productsService.getProductById(id);
  }

  @Post()
  @HttpCode(HttpStatus.CREATED)
  public async createProduct(@Body() data: CreateProductRequest, @Req() req: RequestWithToken) {
    return this.productsService.createProduct(data, req.token.id);
  }

  @Post(':id/option-types')
  @HttpCode(HttpStatus.CREATED)
  public async createOptionType(@Query('id', new ParseUUIDPipe()) productId: string, @Body() data: CreateProductOptionTypeRequest) {
    return this.productsService.createOptionType(productId, data);
  }

  @Get(':id/option-types')
  @HttpCode(HttpStatus.OK)
  public async getOptionTypes(@Query('id', new ParseUUIDPipe()) productId: string) {
    return this.productsService.getOptionTypes(productId);
  }

  @Post('/option-types/:optionTypeId/option-values')
  @HttpCode(HttpStatus.CREATED)
  public async createOptionValue(@Query('optionTypeId', new ParseUUIDPipe()) optionTypeId: string, @Body() data: CreateProductOptionValueRequest) {
    return this.productsService.createOptionValue(optionTypeId, data);
  }

  @Post(':id/variants')
  @HttpCode(HttpStatus.CREATED)
  public async createVariant(@Query('id', new ParseUUIDPipe()) productId: string, @Body() data: CreateProductVariantRequest) {
    return this.productsService.createVariant(productId, data);
  }
}
