import { Controller, Get, HttpCode, HttpStatus, Param, ParseUUIDPipe, Req } from '@nestjs/common';
import { UsersService } from './users.service';
import { ApiBearerAuth, ApiOkResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUserProfileResponse, UserProfileResponse } from './users.model';
import { RequestWithToken } from '../auth/auth.model';

@Controller('users')
@ApiTags('Users')
@ApiBearerAuth()
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get('me')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Get the authenticated user profile' })
  @ApiOkResponse({ type: CurrentUserProfileResponse })
  public async getCurrentUser(@Req() req: RequestWithToken) {
    return this.usersService.getUserById(req.token.id);
  }

  @Get(':id')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Get a user profile by id' })
  @ApiOkResponse({ type: UserProfileResponse })
  public async getUserById(@Param('id', new ParseUUIDPipe()) id: string) {
    return this.usersService.getUserById(id);
  }
}
