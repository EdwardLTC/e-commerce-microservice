import { Module } from '@nestjs/common';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { EnvironmentsModule } from '../environments/environments.module';
import { EnvironmentsService } from '../environments/environments.service';
import { OrdersController } from './orders.controller';
import { OrdersService } from './orders.service';

@Module({
  imports: [
    ClientsModule.registerAsync([
      {
        name: 'com.ecommerce.golang.order',
        imports: [EnvironmentsModule],
        inject: [EnvironmentsService],
        useFactory: (env: EnvironmentsService) => ({
          transport: Transport.GRPC,
          options: {
            package: 'com.ecommerce.golang.order',
            protoPath: join(__dirname, '../../../.proto/Order.proto'),
            url: env.microservice.orderServiceURL,
            loader: {
              defaults: true,
              arrays: true,
              objects: true,
            },
          },
        }),
      },
    ]),
  ],
  controllers: [OrdersController],
  providers: [OrdersService],
  exports: [OrdersService],
})
export class OrdersModule {}
