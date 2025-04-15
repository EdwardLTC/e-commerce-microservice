import { Inject, Injectable } from '@nestjs/common';
import { ClientGrpc } from '@nestjs/microservices';
import { GrpcUserClient } from '../generated/protos/users';
import { lastValueFrom } from 'rxjs';

@Injectable()
export class UsersService {
  private readonly clientGrpc = this.client.getService<GrpcUserClient.UserService>('UserService');

  constructor(@Inject('USER_SERVICE') private client: ClientGrpc) {}

  public async getUserById(id: number) {
    return lastValueFrom(this.clientGrpc.getUserById({ id: { value: id } }));
  }

  public async getUserByEmailAndPassword(email: string, password: string) {
    return lastValueFrom(this.clientGrpc.getUserByEmailAndPassword({ email: { value: email }, password: { value: password } }));
  }

  public async createUser(email: string, password: string, name: string) {
    return lastValueFrom(this.clientGrpc.createUser({ email: { value: email }, password: { value: password }, name: { value: name } }));
  }
}
