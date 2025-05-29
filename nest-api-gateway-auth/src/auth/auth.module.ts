import { Module } from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { CacheModule } from '@nestjs/cache-manager';
import { JwtModule } from '@nestjs/jwt';
import { EnvironmentsService } from '../environments/environments.service';
import { EnvironmentsModule } from '../environments/environments.module';
import { UsersModule } from '../users/users.module';
import { createClient, createKeyv, Keyv, RedisClientType } from '@keyv/redis';

@Module({
  imports: [
    CacheModule.registerAsync({
      imports: [EnvironmentsModule],
      inject: [EnvironmentsService],
      useFactory: async (env: EnvironmentsService) => {
        return {
          stores: [new Keyv(), createKeyv(env.redis.url)],
        };
      },
    }),
    JwtModule.registerAsync({
      global: true,
      imports: [EnvironmentsModule],
      inject: [EnvironmentsService],
      useFactory: async (env: EnvironmentsService) => ({
        secret: env.jwt.secret,
        signOptions: { expiresIn: '7d' },
      }),
    }),
    UsersModule,
  ],
  controllers: [AuthController],
  providers: [
    AuthService,
    {
      provide: 'REDIS_CLIENT',
      inject: [EnvironmentsService],
      useFactory: async (env: EnvironmentsService): Promise<RedisClientType> => {
        const client: RedisClientType = createClient({ url: env.redis.url });
        await client.connect();
        return client;
      },
    },
  ],
  exports: [AuthService],
})
export class AuthModule {}
