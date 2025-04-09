import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JWTConfig, MicroserviceConfig } from './environments.model';

@Injectable()
export class EnvironmentsService {
  public readonly environment = this.configService.get<string>('NODE_ENV');
  public readonly port = this.configService.get<number>('PORT');
  public readonly jwt: JWTConfig = {
    secret: this.configService.get<string>('JWT_SECRET'),
  };
  public readonly isDevelopment = this.environment === 'development';
  public readonly microservice: MicroserviceConfig = {
    userServiceURL: this.configService.get<string>('USER_SERVICE_URL'),
  };

  constructor(private configService: ConfigService) {}
}
