import { Inject, Injectable } from '@nestjs/common';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import { UsersService } from '../users/users.service';
import { JwtService } from '@nestjs/jwt';
import { Cache } from 'cache-manager';
import { ChangePasswordDto, LoginDto, LoginSessionStatus, RegisterDto } from './auth.model';
import { Request } from 'express';
import { RedisClientType } from '@keyv/redis';
import { randomUUID } from 'crypto';

@Injectable()
export class AuthService {
  constructor(
    @Inject(CACHE_MANAGER) private cacheManager: Cache,
    @Inject('REDIS_CLIENT') private redisClient: RedisClientType,
    private userService: UsersService,
    private jwtService: JwtService,
  ) {}

  public async login(loginDto: LoginDto, request: Request) {
    const user = await this.userService.getUserByEmailAndPassword(loginDto.email, loginDto.password);

    const jti = randomUUID();

    const assessToken = this.jwtService.sign({ id: user.id, email: user.email, jti: jti });

    const session = {
      jti: jti,
      userId: user.id,
      status: LoginSessionStatus.ACTIVE,
      ip: request.ip,
      userAgent: request.headers['user-agent'],
      createdAt: new Date(),
      expiredAt: new Date(Date.now() + 60 * 60 * 24 * 7 * 1000),
    };

    await this.cacheManager.set(`session:${jti}`, session, 60 * 60 * 24 * 7);

    await this.redisClient.sAdd(`user:sessions:${user.id}`, `auth:session:${jti}`);

    await this.redisClient.expire(`user:sessions:${user.id}`, 60 * 60 * 24 * 7);

    return {
      access_token: assessToken,
    };
  }

  public async changePassword(userId: string, changePasswordDto: ChangePasswordDto) {
    await this.userService.changePassword(userId, changePasswordDto.oldPassword, changePasswordDto.newPassword);

    const sessions = await this.redisClient.sMembers(`user_sessions:${userId}`);

    await this.cacheManager.mdel(sessions);

    await this.redisClient.del(`user:sessions:${userId}`);
  }

  public async register(registerDto: RegisterDto) {
    return this.userService.createUser(registerDto.email, registerDto.password, registerDto.name);
  }

  public async getSession(jti: string) {
    return this.cacheManager.get<{
      jti: string;
      status: LoginSessionStatus;
    }>(`session:${jti}`);
  }

  public async logout(jit: string) {
    await this.cacheManager.del(`auth:session:${jit}`);
  }
}
