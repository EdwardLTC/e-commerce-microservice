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
      useFactory: async () => {
        return {
          stores: [new Keyv(), createKeyv('redis://localhost:6379', { namespace: 'auth' })],
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
      useFactory: async (): Promise<RedisClientType> => {
        const client: RedisClientType = createClient({ url: 'redis://localhost:6379' });
        await client.connect();
        return client;
      },
    },
  ],
  exports: [AuthService],
})
export class AuthModule {}
