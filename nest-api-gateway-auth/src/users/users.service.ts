import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { UserService } from '../generated/protos/users';
import { lastValueFrom } from 'rxjs';

@Injectable()
export class UsersService {
  private readonly clientGrpc = this.client.getService<UserService>('UserService');
  constructor(@Inject('USER_SERVICE') private client: ClientGrpc) {}

  public async getUserById(id: number) {
    return lastValueFrom(this.clientGrpc.getUserById({ id }));
  }
}
