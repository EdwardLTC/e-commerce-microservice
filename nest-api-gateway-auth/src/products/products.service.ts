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
import { com } from '../../generated/.proto/Product';
import GetProductDetailResponse = com.ecommerce.springboot.product.v1.GetProductDetailResponse;

@Injectable()
export class ProductsService {
  private readonly productClient = this.client.getService<com.ecommerce.springboot.product.v1.ProductService>('ProductService');
  private readonly optionClient = this.client.getService<com.ecommerce.springboot.product.v1.OptionService>('OptionService');
  private readonly variantClient = this.client.getService<com.ecommerce.springboot.product.v1.VariantService>('VariantService');

  constructor(@Inject('PRODUCT_SERVICE') private client: ClientGrpc) {}

  public async getProducts(data: GetProductsRequest) {
    const value = await lastValueFrom(
      this.productClient.getProducts({
        skip: { value: data.skip },
        take: { value: data.take },
      }),
    );

    return value.products;
  }

  public async getProductById(id: string): Promise<GetProductDetailResponse> {
    return lastValueFrom(this.productClient.getProductDetail({ id: { value: id } }));
  }

  public async createProduct(data: CreateProductRequest, sellerId: string) {
    return lastValueFrom(
      this.productClient.createProduct({
        sellerId: { value: sellerId },
        name: { value: data.name },
        brand: { value: data.brand },
        description: { value: data.description },
        mediaUrls: data.mediaUrls,
      }),
    );
  }

  public async createOptionType(productId: string, data: CreateProductOptionTypeRequest) {
    return lastValueFrom(
      this.optionClient.createOptionType({
        productId: { value: productId },
        name: { value: data.name },
        displayOrder: data.displayOrder ? { value: data.displayOrder } : undefined,
      }),
    );
  }

  public async createOptionValue(optionTypeId: string, data: CreateProductOptionValueRequest) {
    return lastValueFrom(
      this.optionClient.createOptionValue({
        optionTypeId: { value: optionTypeId },
        value: { value: data.value },
        mediaUrl: data.mediaUrl ? { value: data.mediaUrl } : undefined,
        displayOrder: data.displayOrder ? { value: data.displayOrder } : undefined,
      }),
    );
  }

  public async createVariant(productId: string, data: CreateProductVariantRequest) {
    return lastValueFrom(
      this.variantClient.createVariant({
        productId: { value: productId },
        sku: { value: data.sku },
        price: { value: data.price },
        stock: { value: data.stock },
        mediaUrl: data.mediaUrl ? { value: data.mediaUrl } : undefined,
        options: data.options,
      }),
    );
  }
}
