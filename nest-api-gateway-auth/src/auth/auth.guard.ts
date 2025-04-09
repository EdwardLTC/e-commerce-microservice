import { CanActivate, ExecutionContext, Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { Reflector } from '@nestjs/core';
import { IS_PUBLIC_KEY, LoginSessionStatus, Token } from './auth.model';
import { EnvironmentsService } from '../environments/environments.service';
import { Request } from 'express';
import { AuthService } from './auth.service';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(
    private jwtService: JwtService,
    private reflector: Reflector,
    private env: EnvironmentsService,
    private authService: AuthService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [context.getHandler(), context.getClass()]);
    if (isPublic) return true;

    const request = context.switchToHttp().getRequest();
    const token = this.extractTokenFromHeader(request);
    if (!token) {
      throw new UnauthorizedException();
    }

    const deserialize = await this.jwtService
      .verifyAsync<Token>(token, {
        secret: this.env.jwt.secret,
      })
      .catch(() => {
        throw new UnauthorizedException();
      });

    const session = await this.authService.getSession(deserialize.jti);

    if (!session) throw new UnauthorizedException();

    if (session.status !== LoginSessionStatus.ACTIVE) throw new UnauthorizedException();

    request.token = deserialize;
    return true;
  }

  private extractTokenFromHeader(request: Request): string | undefined {
    const [type, token] = request.headers.authorization?.split(' ') ?? [];
    return type === 'Bearer' ? token : undefined;
  }
}
