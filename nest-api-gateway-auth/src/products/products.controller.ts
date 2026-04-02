import { Body, Controller, Get, HttpCode, HttpStatus, Param, ParseUUIDPipe, Post, Query, Req } from '@nestjs/common';
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
  public async getProductById(@Param('id', new ParseUUIDPipe()) id: string) {
    return this.productsService.getProductById(id);
  }

  @Post()
  @HttpCode(HttpStatus.CREATED)
  public async createProduct(@Body() data: CreateProductRequest, @Req() req: RequestWithToken) {
    return this.productsService.createProduct(data, req.token.id);
  }

  @Post(':id/option-types')
  @HttpCode(HttpStatus.CREATED)
  public async createOptionType(@Param('id', new ParseUUIDPipe()) productId: string, @Body() data: CreateProductOptionTypeRequest) {
    return this.productsService.createOptionType(productId, data);
  }

  @Get(':id/option-types')
  @HttpCode(HttpStatus.OK)
  public async getOptionTypes(@Param('id', new ParseUUIDPipe()) productId: string) {
    return this.productsService.getOptionTypes(productId);
  }

  @Post('/option-types/:optionTypeId/option-values')
  @HttpCode(HttpStatus.CREATED)
  public async createOptionValue(@Param('optionTypeId', new ParseUUIDPipe()) optionTypeId: string, @Body() data: CreateProductOptionValueRequest) {
    return this.productsService.createOptionValue(optionTypeId, data);
  }

  @Get('/option-types/:optionTypeId/option-values')
  @HttpCode(HttpStatus.OK)
  public async getOptionValues(@Param('optionTypeId', new ParseUUIDPipe()) optionTypeId: string) {
    return this.productsService.getOptionValues(optionTypeId);
  }

  @Post(':id/variants')
  @HttpCode(HttpStatus.CREATED)
  public async createVariant(@Param('id', new ParseUUIDPipe()) productId: string, @Body() data: CreateProductVariantRequest) {
    return this.productsService.createVariant(productId, data);
  }
}
