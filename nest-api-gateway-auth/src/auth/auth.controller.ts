import { Body, Controller, HttpCode, HttpStatus, Post, Req } from '@nestjs/common';
import { AuthService } from './auth.service';
import {
  AuthTokenResponse,
  ChangePasswordDto,
  LoginDto,
  Public,
  RegisterDto,
  RequestWithToken,
  UserProfileResponse,
} from './auth.model';
import { ApiBearerAuth, ApiCreatedResponse, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Request } from 'express';

@Controller('auth')
@ApiTags('Auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Public()
  @HttpCode(HttpStatus.OK)
  @Post('login')
  @ApiOperation({ summary: 'Login and create a session' })
  @ApiOkResponse({ type: AuthTokenResponse })
  public async login(@Body() loginDto: LoginDto, @Req() req: Request) {
    return this.authService.login(loginDto, req);
  }

  @Public()
  @HttpCode(HttpStatus.CREATED)
  @Post('register')
  @ApiOperation({ summary: 'Register a new user account' })
  @ApiCreatedResponse({ type: UserProfileResponse })
  public async register(@Body() registerDto: RegisterDto) {
    return this.authService.register(registerDto);
  }

  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth()
  @Post('logout')
  @ApiOperation({ summary: 'Logout and invalidate the current session' })
  @ApiOkResponse({ schema: { example: null } })
  public async logout(@Req() req: RequestWithToken) {
    return this.authService.logout(req.token.jti);
  }

  @HttpCode(HttpStatus.OK)
  @ApiBearerAuth()
  @Post('change-password')
  @ApiOperation({ summary: 'Change the current user password' })
  @ApiOkResponse({ schema: { example: null } })
  public async changePassword(@Body() changePasswordDto: ChangePasswordDto, @Req() req: RequestWithToken) {
    return this.authService.changePassword(req.token.id, changePasswordDto);
  }
}
