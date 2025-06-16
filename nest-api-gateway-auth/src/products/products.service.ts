import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { CreateProductRequest, GetProductsRequest, GetProductsResponse } from './products.model';
import { lastValueFrom } from 'rxjs';
import { com } from '../../generated/.proto/Product';

@Injectable()
export class ProductsService {
  private readonly clientGrpc = this.client.getService<com.ecommerce.springboot.product.v1.ProductService>('ProductService');

  constructor(@Inject('PRODUCT_SERVICE') private client: ClientGrpc) {}

  public async getProducts(data: GetProductsRequest): Promise<GetProductsResponse[]> {
    const value = await lastValueFrom(
      this.clientGrpc.getProducts({
        skip: { value: data.skip },
        take: { value: data.take },
      }),
    );

    return value.products;
  }

  public async createProduct(data: CreateProductRequest, sellerId: string): Promise<GetProductsResponse> {
    return await lastValueFrom(
      this.clientGrpc.createProduct({
        sellerId: { value: sellerId },
        name: { value: data.name },
        brand: { value: data.brand },
        description: { value: data.description },
        mediaUrls: data.mediaUrls,
      }),
    );
  }
}
