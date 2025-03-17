import { ArgumentsHost, Catch, ExceptionFilter, HttpException, HttpStatus, Logger } from '@nestjs/common';
import { Request, Response } from 'express';
import { GrpcClientException } from './grpc-client.exception';

@Catch()
export class GlobalExceptionsFilter implements ExceptionFilter {
  private logger = new Logger(GlobalExceptionsFilter.name);

  catch(exception: any, host: ArgumentsHost) {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    let responseBody: { statusCode: number; timestamp: Date; message: string; cause: unknown } = {
      statusCode: HttpStatus.INTERNAL_SERVER_ERROR,
      message: 'Something went wrong',
      timestamp: new Date(),
      cause: null,
    };

    if (exception instanceof GrpcClientException) exception = exception.toHttpException();

    if (exception instanceof HttpException) {
      responseBody = {
        statusCode: exception.getStatus(),
        timestamp: new Date(),
        message: exception.getStatus() === HttpStatus.INTERNAL_SERVER_ERROR ? 'Something went wrong' : exception.getResponse()['message'],
        cause: exception.getStatus() >= HttpStatus.INTERNAL_SERVER_ERROR ? null : (exception.cause ?? null),
      };
    }

    this.logger.error(`[${request.method}] ${request.path} >> Message:: ${responseBody.message}`, exception);

    response.status(responseBody.statusCode).json(responseBody);
  }
}
