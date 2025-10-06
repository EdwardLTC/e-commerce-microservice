import { InterceptingCall, Interceptor, InterceptorOptions, NextCall, Requester } from '@grpc/grpc-js';
import { Injectable } from '@nestjs/common';

@Injectable()
export class EncodeGrpcRequestInterceptor {
  static create(): Interceptor {
    return (options: InterceptorOptions, nextCall: NextCall): InterceptingCall => {
      const requester: Requester = {
        sendMessage: (message, next) => {
          // try {
          //   if (message?.constructor?.encode && message.constructor.decode) {
          //     const encoded = message.constructor.encode(message).finish();
          //     const decoded = message.constructor.decode(encoded);
          //     console.log(`[gRPC Client] Sending → ${options.method_definition.path}`, decoded);
          //   } else {
          //     console.log(`[gRPC Client] Sending no → ${options.method_definition.path}`, message);
          //   }
          // } catch (err) {
          //   console.warn('[gRPC Client] Failed to decode request', err);
          // }
          console.log(`[gRPC Client] Sending → ${options.method_definition.path}`, message);
          next(message);
        },
      };

      return new InterceptingCall(nextCall(options), requester);
    };
  }
}
