import { Module } from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { CacheModule } from '@nestjs/cache-manager';
import { JwtModule } from '@nestjs/jwt';
import { EnvironmentsService } from '../environments/environments.service';
import { EnvironmentsModule } from '../environments/environments.module';
import { UsersModule } from '../users/users.module';
import { createKeyv, Keyv } from '@keyv/redis';

@Module({
  imports: [
    CacheModule.registerAsync({
      useFactory: async () => {
        return {
          stores: [new Keyv(), createKeyv('redis://localhost:6379')],
        };
      },
    }),
    JwtModule.registerAsync({
      global: true,
      imports: [EnvironmentsModule],
      useFactory: async (env: EnvironmentsService) => ({
        secret: env.jwt.secret,
        signOptions: { expiresIn: '7d' },
      }),
      inject: [EnvironmentsService],
    }),
    UsersModule,
  ],
  controllers: [AuthController],
  providers: [AuthService],
  exports: [AuthService],
})
export class AuthModule {}
