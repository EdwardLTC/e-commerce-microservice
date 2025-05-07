import { Body, Controller, HttpCode, HttpStatus, Post, Req } from '@nestjs/common';
import { AuthService } from './auth.service';
import { ChangePasswordDto, LoginDto, Public, RegisterDto, RequestWithToken } from './auth.model';
import { ApiBearerAuth } from '@nestjs/swagger';
import { Request } from 'express';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @HttpCode(HttpStatus.OK)
  @Post('login')
  public async login(@Body() loginDto: LoginDto, @Req() req: Request) {
    return this.authService.login(loginDto, req);
  }

  @Public()
  @HttpCode(HttpStatus.CREATED)
  @Post('register')
  public async register(@Body() registerDto: RegisterDto) {
    return this.authService.register(registerDto);
  }

  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth()
  @Post('logout')
  public async logout(@Req() req: RequestWithToken) {
    return this.authService.logout(req.token.jti);
  }

  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth()
  @Post('change-password')
  public async changePassword(@Body() changePasswordDto: ChangePasswordDto, @Req() req: RequestWithToken) {
    return this.authService.changePassword(req.token.id, changePasswordDto);
  }
}
