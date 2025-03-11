namespace asp_user.Extensions;

public static class HttpContextExtensions
{
    public static string? GetJIT(this HttpContext context)
    {
        return context.Items["JTI"]?.ToString();
    }
}