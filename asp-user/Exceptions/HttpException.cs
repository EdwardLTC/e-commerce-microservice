using System.Net;

namespace asp_user.exceptions;

public class HttpException(HttpStatusCode statusCode, string message) : Exception(message)
{
    public new string Message => message;
    public HttpStatusCode StatusCode => statusCode;
}