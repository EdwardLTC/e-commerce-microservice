import { Inject, Injectable } from '@nestjs/common';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import { UsersService } from '../users/users.service';
import { JwtService } from '@nestjs/jwt';
import { Cache } from 'cache-manager';
import { LoginDto, LoginSessionStatus, RegisterDto } from './auth.model';

@Injectable()
export class AuthService {
  constructor(
    @Inject(CACHE_MANAGER) private cacheManager: Cache,
    private userService: UsersService,
    private jwtService: JwtService,
  ) {}

  public async login(loginDto: LoginDto) {
    const user = await this.userService.getUserByEmailAndPassword(loginDto.email, loginDto.password);

    const jti = crypto.randomUUID();

    const assessToken = this.jwtService.sign({ id: user.id, email: user.email, jti: jti });

    const session = {
      jti: jti,
      status: LoginSessionStatus.ACTIVE,
    };

    await this.cacheManager.set(`session_${jti}`, session, 60 * 60 * 24 * 7);

    return {
      access_token: assessToken,
    };
  }

  public async register(registerDto: RegisterDto) {
    return this.userService.createUser(registerDto.email, registerDto.password, registerDto.name);
  }

  public async getSession(jti: string) {
    return this.cacheManager.get<{
      jti: string;
      status: LoginSessionStatus;
    }>(`session_${jti}`);
  }

  public async logout(jit: string) {
    await this.cacheManager.del(`session_${jit}`);
  }
}
