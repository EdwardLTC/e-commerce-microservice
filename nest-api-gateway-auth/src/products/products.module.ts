import { Module } from '@nestjs/common';
import { ProductsService } from './products.service';
import { ProductsController } from './products.controller';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { EnvironmentsModule } from '../environments/environments.module';
import { EnvironmentsService } from '../environments/environments.service';
import { join } from 'path';
import { COM_ECOMMERCE_SPRINGBOOT_PRODUCT_V1_PACKAGE_NAME } from '../generated/Product';

@Module({
  imports: [
    ClientsModule.registerAsync([
      {
        name: COM_ECOMMERCE_SPRINGBOOT_PRODUCT_V1_PACKAGE_NAME,
        imports: [EnvironmentsModule],
        inject: [EnvironmentsService],
        useFactory: (env: EnvironmentsService) => ({
          transport: Transport.GRPC,
          options: {
            package: COM_ECOMMERCE_SPRINGBOOT_PRODUCT_V1_PACKAGE_NAME,
            protoPath: join(__dirname, '../../../.proto/Product.proto'),
            url: env.microservice.productServiceURL,
            loader: {
              defaults: true,
              arrays: true,
            },
          },
        }),
      },
    ]),
  ],
  controllers: [ProductsController],
  providers: [ProductsService],
})
export class ProductsModule {}
