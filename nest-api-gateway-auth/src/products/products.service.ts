import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import {
  CreateProductOptionTypeRequest,
  CreateProductOptionValueRequest,
  CreateProductRequest,
  CreateProductVariantRequest,
  GetProductsRequest,
} from './products.model';
import { lastValueFrom } from 'rxjs';
import { com } from '../generated/.proto/Product';
import { numberValue, stringValue } from '../helpers/well-know-type';
import ProductService = com.ecommerce.springboot.product.v1.ProductService;
import OptionService = com.ecommerce.springboot.product.v1.OptionService;
import VariantService = com.ecommerce.springboot.product.v1.VariantService;

@Injectable()
export class ProductsService {
  private readonly productClient = this.client.getService<ProductService>('ProductService');
  private readonly optionClient = this.client.getService<OptionService>('OptionService');
  private readonly variantClient = this.client.getService<VariantService>('VariantService');

  constructor(@Inject('com.ecommerce.springboot.product.v1') private client: ClientGrpc) {}

  public async getProducts(data: GetProductsRequest) {
    return lastValueFrom(
      this.productClient.getProducts({
        skip: numberValue(data.skip),
        take: numberValue(data.take),
      }),
    ).then(response => response.products);
  }

  public async getProductById(id: string) {
    return lastValueFrom(this.productClient.getProductDetail({ id: stringValue(id) }));
  }

  public async createProduct(data: CreateProductRequest, sellerId: string) {
    return lastValueFrom(
      this.productClient.createProduct({
        sellerId: stringValue(sellerId),
        name: stringValue(data.name),
        brand: stringValue(data.brand),
        description: stringValue(data.description),
        mediaUrls: data.mediaUrls,
      }),
    );
  }

  public async getOptionTypes(productId: string) {
    return lastValueFrom(this.optionClient.getOptionTypes({ productId: stringValue(productId) })).then(response => response.optionTypes);
  }

  public async createOptionType(productId: string, data: CreateProductOptionTypeRequest) {
    return lastValueFrom(
      this.optionClient.createOptionType({
        productId: stringValue(productId),
        name: stringValue(data.name),
        displayOrder: data.displayOrder ? numberValue(data.displayOrder) : undefined,
      }),
    );
  }

  public async getOptionValues(optionTypeId: string) {
    return lastValueFrom(this.optionClient.getOptionValues({ optionTypeId: stringValue(optionTypeId) })).then(response => response.optionValues);
  }

  public async createOptionValue(optionTypeId: string, data: CreateProductOptionValueRequest) {
    return lastValueFrom(
      this.optionClient.createOptionValue({
        optionTypeId: stringValue(optionTypeId),
        value: stringValue(data.value),
        mediaUrl: data.mediaUrl ? stringValue(data.mediaUrl) : undefined,
        displayOrder: data.displayOrder ? numberValue(data.displayOrder) : undefined,
      }),
    );
  }

  public async createVariant(productId: string, data: CreateProductVariantRequest) {
    return lastValueFrom(
      this.variantClient.createVariant({
        productId: stringValue(productId),
        sku: stringValue(data.sku),
        price: numberValue(data.price),
        stock: numberValue(data.stock),
        mediaUrl: data.mediaUrl ? stringValue(data.mediaUrl) : undefined,
        options: data.options,
      }),
    );
  }
}
