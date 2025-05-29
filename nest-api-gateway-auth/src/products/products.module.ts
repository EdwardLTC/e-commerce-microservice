import { Module } from '@nestjs/common';
import { ProductsService } from './products.service';
import { ProductsController } from './products.controller';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { EnvironmentsModule } from '../environments/environments.module';
import { EnvironmentsService } from '../environments/environments.service';
import { join } from 'path';

@Module({
  imports: [
    ClientsModule.registerAsync([
      {
        name: 'PRODUCT_SERVICE',
        imports: [EnvironmentsModule],
        inject: [EnvironmentsService],
        useFactory: (env: EnvironmentsService) => ({
          transport: Transport.GRPC,
          options: {
            package: 'com.ecommerce.springboot.product.v1',
            protoPath: join(__dirname, '../products/products.proto'),
            url: env.microservice.productServiceURL,
          },
        }),
      },
    ]),
  ],
  controllers: [ProductsController],
  providers: [ProductsService],
})
export class ProductsModule {}
