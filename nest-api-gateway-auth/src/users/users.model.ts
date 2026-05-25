import { ApiProperty } from '@nestjs/swagger';

export class UserProfileResponse {
  @ApiProperty({
    example: 'b1db89c1-8f78-486c-80db-ddf64b75b8f0',
  })
  id: string;

  @ApiProperty({
    example: 'John Doe',
  })
  name: string;

  @ApiProperty({
    example: 'john.doe@example.com',
  })
  email: string;
}

export class CurrentUserProfileResponse extends UserProfileResponse {}
