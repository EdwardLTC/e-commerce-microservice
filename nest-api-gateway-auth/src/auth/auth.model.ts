import { ApiProperty } from '@nestjs/swagger';
import { SetMetadata } from '@nestjs/common';
import { IsEmail, IsNotEmpty, IsString, MinLength } from 'class-validator';

export const IS_PUBLIC_KEY = 'isPublic';
export const Public = () => SetMetadata(IS_PUBLIC_KEY, true);

export enum LoginSessionStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
}
export type Token = {
  id: string;
  email: string;
  jti: string;
};
export type RequestWithToken = {
  token: Token;
};

export class LoginDto {
  @ApiProperty({
    example: 'lethanhcong@gmail.com',
  })
  @IsEmail()
  email: string;

  @ApiProperty({
    example: '123456789',
  })
  @IsString()
  @IsNotEmpty()
  password: string;
}

export class RegisterDto {
  @ApiProperty({
    example: 'lethanhcong@gmail.com',
  })
  @IsEmail()
  email: string;

  @ApiProperty({
    example: '123456789',
  })
  @IsString()
  @IsNotEmpty()
  password: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  name: string;
}

export class ChangePasswordDto {
  @ApiProperty({
    example: '123456789',
  })
  @IsString()
  @IsNotEmpty()
  @MinLength(6)
  oldPassword: string;

  @ApiProperty({
    example: '123456789',
  })
  @IsString()
  @IsNotEmpty()
  @MinLength(6)
  newPassword: string;
}

export class AuthTokenResponse {
  @ApiProperty({
    example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9',
  })
  access_token: string;
}

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
