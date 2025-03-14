import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { IUserService } from '../generated/protos/users';

@Injectable()
export class UsersService {
  private readonly clientGrpc = this.client.getService<IUserService>('IUserService');
  constructor(@Inject('USER_SERVICE') private client: ClientGrpc) {}

  public async getUserById(id: number) {
    return this.clientGrpc.getUserById({ id });
  }
}
