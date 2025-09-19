import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { lastValueFrom } from 'rxjs';
import { COM_ECOMMERCE_ASPNET_USER_PACKAGE_NAME, USER_SERVICE_NAME, UserServiceClient } from '../generated/User';

@Injectable()
export class UsersService {
  private readonly clientGrpc = this.client.getService<UserServiceClient>(USER_SERVICE_NAME);

  constructor(@Inject(COM_ECOMMERCE_ASPNET_USER_PACKAGE_NAME) private client: ClientGrpc) {}

  public async getUserByEmailAndPassword(email: string, password: string) {
    return lastValueFrom(this.clientGrpc.getUserByEmailAndPassword({ email: email, password: password }));
  }

  public async createUser(email: string, password: string, name: string) {
    return lastValueFrom(this.clientGrpc.createUser({ email: email, password: password, name: name }));
  }

  public async changePassword(id: string, oldPassword: string, newPassword: string) {
    return lastValueFrom(this.clientGrpc.changePassword({ id: id, oldPassword: oldPassword, newPassword: newPassword }));
  }
}
