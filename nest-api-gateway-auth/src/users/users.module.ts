import { Module } from '@nestjs/common';
import { UsersService } from './users.service';
import { UsersController } from './users.controller';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { EnvironmentsModule } from '../environments/environments.module';
import { EnvironmentsService } from '../environments/environments.service';
import { COM_ECOMMERCE_ASPNET_USER_PACKAGE_NAME } from '../generated/User';

@Module({
  imports: [
    ClientsModule.registerAsync([
      {
        name: COM_ECOMMERCE_ASPNET_USER_PACKAGE_NAME,
        imports: [EnvironmentsModule],
        inject: [EnvironmentsService],
        useFactory: (env: EnvironmentsService) => ({
          transport: Transport.GRPC,
          options: {
            package: COM_ECOMMERCE_ASPNET_USER_PACKAGE_NAME,
            protoPath: join(__dirname, '../../../.proto/User.proto'),
            url: env.microservice.userServiceURL,
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
  controllers: [UsersController],
  providers: [UsersService],
  exports: [UsersService],
})
export class UsersModule {}
