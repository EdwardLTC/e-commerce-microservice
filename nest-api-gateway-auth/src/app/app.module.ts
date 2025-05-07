import { Module } from '@nestjs/common';
import { UsersModule } from '../users/users.module';
import { EnvironmentsModule } from '../environments/environments.module';
import { APP_FILTER, APP_GUARD, APP_INTERCEPTOR } from '@nestjs/core';
import { GrpcInterceptor } from '../interceptors/grpc.interceptor';
import { GlobalExceptionsFilter } from '../exceptions/global-exceptions.filter';
import { AuthModule } from '../auth/auth.module';
import { AuthGuard } from '../auth/auth.guard';

@Module({
    imports: [EnvironmentsModule, UsersModule, AuthModule],
    providers: [
        {
            provide: APP_INTERCEPTOR,
            useClass: GrpcInterceptor,
        },
        {
            provide: APP_FILTER,
            useClass: GlobalExceptionsFilter,
        },
        {
            provide: APP_GUARD,
            useClass: AuthGuard,
        },
    ],
})
export class AppModule {
}
