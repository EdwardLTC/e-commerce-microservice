using System.Net;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace asp_user.exceptions;

public class GlobalExceptionFilter(ILogger<GlobalExceptionFilter> logger) : IExceptionFilter
{
    public void OnException(ExceptionContext context)
    {
        if (context.Exception is HttpException httpException && httpException.StatusCode != HttpStatusCode.InternalServerError)
        {
            context.Result = new ObjectResult(new
            {
                httpException.Message,
                httpException.StatusCode
            });
            context.HttpContext.Response.StatusCode = (int)httpException.StatusCode;
            context.ExceptionHandled = true;
            return;
        }

        logger.LogError(context.Exception, "An unhandled exception occurred");
        
        context.Result = new ObjectResult(new
        {
            Message = "An error occurred while processing your request",
            StatusCode = HttpStatusCode.InternalServerError
        });
        context.HttpContext.Response.StatusCode = (int)HttpStatusCode.InternalServerError;
        context.ExceptionHandled = true;
    }
}