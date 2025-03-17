import { Status } from '@grpc/grpc-js/build/src/constants';
import { HttpException, HttpStatus } from '@nestjs/common';

export const HTTP_CODE_FROM_GRPC: Record<number, number> = {
  [Status.OK]: HttpStatus.OK,
  [Status.CANCELLED]: HttpStatus.METHOD_NOT_ALLOWED,
  [Status.UNKNOWN]: HttpStatus.BAD_GATEWAY,
  [Status.INVALID_ARGUMENT]: HttpStatus.UNPROCESSABLE_ENTITY,
  [Status.DEADLINE_EXCEEDED]: HttpStatus.REQUEST_TIMEOUT,
  [Status.NOT_FOUND]: HttpStatus.NOT_FOUND,
  [Status.ALREADY_EXISTS]: HttpStatus.CONFLICT,
  [Status.PERMISSION_DENIED]: HttpStatus.FORBIDDEN,
  [Status.RESOURCE_EXHAUSTED]: HttpStatus.TOO_MANY_REQUESTS,
  [Status.FAILED_PRECONDITION]: HttpStatus.PRECONDITION_REQUIRED,
  [Status.ABORTED]: HttpStatus.METHOD_NOT_ALLOWED,
  [Status.OUT_OF_RANGE]: HttpStatus.PAYLOAD_TOO_LARGE,
  [Status.UNIMPLEMENTED]: HttpStatus.NOT_IMPLEMENTED,
  [Status.INTERNAL]: HttpStatus.INTERNAL_SERVER_ERROR,
  [Status.UNAVAILABLE]: HttpStatus.SERVICE_UNAVAILABLE,
  [Status.DATA_LOSS]: HttpStatus.INTERNAL_SERVER_ERROR,
  [Status.UNAUTHENTICATED]: HttpStatus.UNAUTHORIZED,
};

export const HTTP_OVER_500_MESSAGE_FROM_HTTP_STATUS_CODE: Record<number, string> = {
  [HttpStatus.INTERNAL_SERVER_ERROR]: 'Internal server error',
  [HttpStatus.NOT_IMPLEMENTED]: 'Not implemented',
  [HttpStatus.BAD_GATEWAY]: 'Bad gateway',
  [HttpStatus.SERVICE_UNAVAILABLE]: 'Service unavailable',
  [HttpStatus.GATEWAY_TIMEOUT]: 'Gateway timeout',
  [HttpStatus.INSUFFICIENT_STORAGE]: 'Insufficient storage',
  [HttpStatus.LOOP_DETECTED]: 'Loop detected',
};

export class GrpcClientException extends Error {
  constructor(
    public readonly controller: string,
    public readonly handler: string,
    public readonly code: Status,
    public readonly details: string,
    public readonly metadata: Record<string, any>,
  ) {
    super(details);
    this.name = 'GrpcClientException';
  }

  public toHttpException(): HttpException {
    const httpCode = HTTP_CODE_FROM_GRPC[this.code] ?? HttpStatus.INTERNAL_SERVER_ERROR;
    const message = HTTP_OVER_500_MESSAGE_FROM_HTTP_STATUS_CODE[httpCode] ?? this.details;
    return new HttpException(
      {
        message: message,
        controller: this.controller,
        handler: this.handler,
        code: this.code,
        details: this.details,
      },
      httpCode,
      { cause: this.metadata },
    );
  }
}
