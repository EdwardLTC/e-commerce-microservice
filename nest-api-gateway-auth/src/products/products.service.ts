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
import {
  COM_ECOMMERCE_SPRINGBOOT_PRODUCT_V1_PACKAGE_NAME,
  OPTION_SERVICE_NAME,
  OptionServiceClient,
  PRODUCT_SERVICE_NAME,
  ProductServiceClient,
  VARIANT_SERVICE_NAME,
  VariantServiceClient,
} from '../generated/Product';

@Injectable()
export class ProductsService {
  private readonly productClient = this.client.getService<ProductServiceClient>(PRODUCT_SERVICE_NAME);
  private readonly optionClient = this.client.getService<OptionServiceClient>(OPTION_SERVICE_NAME);
  private readonly variantClient = this.client.getService<VariantServiceClient>(VARIANT_SERVICE_NAME);

  constructor(@Inject(COM_ECOMMERCE_SPRINGBOOT_PRODUCT_V1_PACKAGE_NAME) private client: ClientGrpc) {}

  public async getProducts(data: GetProductsRequest) {
    return lastValueFrom(this.productClient.getProducts(data)).then(response => response.products);
  }

  public async getProductById(id: string) {
    return lastValueFrom(this.productClient.getProductDetail({ id: id }));
  }

  public async createProduct(data: CreateProductRequest, sellerId: string) {
    return lastValueFrom(
      this.productClient.createProduct({
        sellerId: sellerId,
        name: data.name,
        brand: data.brand,
        description: data.description,
        mediaUrls: data.mediaUrls,
      }),
    );
  }

  public async getOptionTypes(productId: string) {
    return lastValueFrom(this.optionClient.getOptionTypes({ productId: productId })).then(response => response.optionTypes);
  }

  public async createOptionType(productId: string, data: CreateProductOptionTypeRequest) {
    return lastValueFrom(
      this.optionClient.createOptionType({
        productId: productId,
        name: data.name,
        displayOrder: data.displayOrder ? data.displayOrder : undefined,
      }),
    );
  }

  public async getOptionValues(optionTypeId: string) {
    return lastValueFrom(this.optionClient.getOptionValues({ optionTypeId: optionTypeId })).then(response => response.optionValues);
  }

  public async createOptionValue(optionTypeId: string, data: CreateProductOptionValueRequest) {
    return lastValueFrom(
      this.optionClient.createOptionValue({
        optionTypeId: optionTypeId,
        value: data.value,
        mediaUrl: data.mediaUrl ? data.mediaUrl : undefined,
        displayOrder: data.displayOrder ? data.displayOrder : undefined,
      }),
    );
  }

  public async createVariant(productId: string, data: CreateProductVariantRequest) {
    return lastValueFrom(
      this.variantClient.createVariant({
        productId: productId,
        sku: data.sku,
        price: data.price,
        stock: data.stock,
        mediaUrl: data.mediaUrl ? data.mediaUrl : undefined,
        options: data.options,
      }),
    );
  }
}
