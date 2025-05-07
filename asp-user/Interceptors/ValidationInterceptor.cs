using FluentValidation;
using Grpc.Core;
using Grpc.Core.Interceptors;

namespace asp_user.Interceptors;

public class ValidationInterceptor(IServiceProvider serviceProvider) : Interceptor
{
    public override async Task<TResponse> UnaryServerHandler<TRequest, TResponse>(
        TRequest request,
        ServerCallContext context,
        UnaryServerMethod<TRequest, TResponse> continuation)
    {
        var validatorType = typeof(IValidator<>).MakeGenericType(request.GetType());
        var validator = serviceProvider.GetService(validatorType);

        if (validator is IValidator<TRequest> typedValidator)
        {
            var result = await typedValidator.ValidateAsync(request, context.CancellationToken);
            if (!result.IsValid)
            {
                var errors = string.Join("; ", result.Errors.Select(e => e.ErrorMessage));
                throw new RpcException(new Status(StatusCode.InvalidArgument, errors));
            }
        }


        return await continuation(request, context);
    }
}