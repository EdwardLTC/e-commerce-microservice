import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { lastValueFrom } from 'rxjs';
import { com } from '../generated/.proto/User';
import { stringValue } from '../helpers/well-know-type';
import UserService = com.ecommerce.aspnet.user.UserService;

@Injectable()
export class UsersService {
  private readonly clientGrpc = this.client.getService<UserService>('UserService');

  constructor(@Inject('com.ecommerce.aspnet.user') private client: ClientGrpc) {}

  public async getUserByEmailAndPassword(email: string, password: string) {
    return lastValueFrom(this.clientGrpc.getUserByEmailAndPassword({ email: stringValue(email), password: stringValue(password) }));
  }

  public async createUser(email: string, password: string, name: string) {
    return lastValueFrom(this.clientGrpc.createUser({ email: stringValue(email), password: stringValue(password), name: stringValue(name) }));
  }

  public async changePassword(id: string, oldPassword: string, newPassword: string) {
    return lastValueFrom(
      this.clientGrpc.changePassword({ id: stringValue(id), oldPassword: stringValue(oldPassword), newPassword: stringValue(newPassword) }),
    );
  }
}
