import { Module } from '@nestjs/common';
import { UsersService } from './users.service';
import { UsersController } from './users.controller';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { join } from 'path';
import { EnvironmentsModule } from '../environments/environments.module';
import { EnvironmentsService } from '../environments/environments.service';

@Module({
  imports: [
    ClientsModule.registerAsync([
      {
        name: 'USER_SERVICE',
        imports: [EnvironmentsModule],
        inject: [EnvironmentsService],
        useFactory: (env: EnvironmentsService) => ({
          transport: Transport.GRPC,
          options: {
            package: 'com.ecommerce.aspnet.user',
            protoPath: join(__dirname, '../protos/users.proto'),
            url: env.microservice.userServiceURL,
          },
        }),
      },
    ]),
  ],
  controllers: [UsersController],
  providers: [UsersService],
  exports: [UsersService],
})
export class UsersModule {
}
