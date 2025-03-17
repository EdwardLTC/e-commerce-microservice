import { Module } from '@nestjs/common';
import { UsersModule } from '../users/users.module';
import { EnvironmentsModule } from '../environments/environments.module';
import { APP_FILTER, APP_INTERCEPTOR } from '@nestjs/core';
import { GrpcInterceptor } from '../interceptors/grpc.interceptor';
import { GlobalExceptionsFilter } from '../exceptions/global-exceptions.filter';

@Module({
  imports: [EnvironmentsModule, UsersModule],
  providers: [
    {
      provide: APP_INTERCEPTOR,
      useClass: GrpcInterceptor,
    },
    {
      provide: APP_FILTER,
      useClass: GlobalExceptionsFilter,
    },
  ],
})
export class AppModule {}
