import { CallHandler, ExecutionContext, Injectable, NestInterceptor } from '@nestjs/common';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { GrpcClientException } from '../exceptions/grpc-client.exception';

@Injectable()
export class GrpcInterceptor implements NestInterceptor {
  intercept(context: ExecutionContext, next: CallHandler): Observable<any> {
    const controller = context.getClass().name;
    const handler = context.getHandler().name;

    return next.handle().pipe(
      catchError(err => {
        if (!this.isValidGrpcError(err)) return throwError(() => err);

        const exception = new GrpcClientException(controller, handler, err.code, err.details);

        return throwError(() => exception);
      }),
    );
  }

  private isValidGrpcError(err: any): boolean {
    return typeof err === 'object' && 'details' in err && typeof err.details === 'string' && 'code' in err && typeof err.code === 'number';
  }
}
