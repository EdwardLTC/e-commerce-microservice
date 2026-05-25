import { Body, Controller, Get, HttpCode, HttpStatus, Param, ParseUUIDPipe, Post, Query, Req } from '@nestjs/common';
import { ProductsService } from './products.service';
import { ApiBearerAuth, ApiCreatedResponse, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import {
  CreateProductOptionTypeRequest,
  CreateProductOptionValueRequest,
  CreateProductRequest,
  CreateProductVariantRequest,
  GetProductsRequest,
  GetVariantsByIdsRequest,
  OptionTypeResponse,
  OptionValueResponse,
  ProductDetailResponse,
  ProductListItemResponse,
  ResourceCreatedResponse,
  VariantListResponse,
  VariantResponse,
} from './products.model';
import { RequestWithToken } from '../auth/auth.model';

@Controller('products')
@ApiBearerAuth()
@ApiTags('Products')
export class ProductsController {
  constructor(private readonly productsService: ProductsService) {}

  @Get()
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'List products' })
  @ApiOkResponse({ type: ProductListItemResponse, isArray: true })
  public async getProducts(@Query() data: GetProductsRequest) {
    return this.productsService.getProducts(data);
  }

  @Get('variants')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Get variants by ids' })
  @ApiOkResponse({ type: VariantListResponse })
  public async getVariantsByIds(@Query() query: GetVariantsByIdsRequest) {
    return this.productsService.getVariantsByIds(query);
  }

  @Get(':id')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Get product detail by id' })
  @ApiOkResponse({ type: ProductDetailResponse })
  public async getProductById(@Param('id', new ParseUUIDPipe()) id: string) {
    return this.productsService.getProductById(id);
  }

  @Post()
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create a product' })
  @ApiCreatedResponse({ type: ResourceCreatedResponse })
  public async createProduct(@Body() data: CreateProductRequest, @Req() req: RequestWithToken) {
    return this.productsService.createProduct(data, req.token.id);
  }

  @Post(':id/option-types')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create an option type for a product' })
  @ApiCreatedResponse({ type: ResourceCreatedResponse })
  public async createOptionType(@Param('id', new ParseUUIDPipe()) productId: string, @Body() data: CreateProductOptionTypeRequest) {
    return this.productsService.createOptionType(productId, data);
  }

  @Get(':id/option-types')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'List option types for a product' })
  @ApiOkResponse({ type: OptionTypeResponse, isArray: true })
  public async getOptionTypes(@Param('id', new ParseUUIDPipe()) productId: string) {
    return this.productsService.getOptionTypes(productId);
  }

  @Post('/option-types/:optionTypeId/option-values')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create an option value for an option type' })
  @ApiCreatedResponse({ type: ResourceCreatedResponse })
  public async createOptionValue(@Param('optionTypeId', new ParseUUIDPipe()) optionTypeId: string, @Body() data: CreateProductOptionValueRequest) {
    return this.productsService.createOptionValue(optionTypeId, data);
  }

  @Get('/option-types/:optionTypeId/option-values')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'List option values for an option type' })
  @ApiOkResponse({ type: OptionValueResponse, isArray: true })
  public async getOptionValues(@Param('optionTypeId', new ParseUUIDPipe()) optionTypeId: string) {
    return this.productsService.getOptionValues(optionTypeId);
  }

  @Post(':id/variants')
  @HttpCode(HttpStatus.CREATED)
  @ApiOperation({ summary: 'Create a product variant' })
  @ApiCreatedResponse({ type: ResourceCreatedResponse })
  public async createVariant(@Param('id', new ParseUUIDPipe()) productId: string, @Body() data: CreateProductVariantRequest) {
    return this.productsService.createVariant(productId, data);
  }
}
