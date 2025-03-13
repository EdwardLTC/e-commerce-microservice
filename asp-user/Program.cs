using System.Reflection;
using asp_user.Contexts;
using asp_user.exceptions;
using asp_user.Extensions;
using asp_user.Kafka;
using Confluent.Kafka;
using Microsoft.EntityFrameworkCore;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

builder.Logging.ClearProviders();
builder.Logging.AddConsole();
builder.Services.AddOpenApi();
builder.Services.AddEndpointsApiExplorer();

builder.Services.Configure<ProducerConfig>(builder.Configuration.GetSection("Kafka:Producer"));
builder.Services.Configure<ConsumerConfig>(builder.Configuration.GetSection("Kafka:Consumer"));

builder.Services.AddAutoRegisteredServices(Assembly.GetExecutingAssembly());
builder.Services.AddControllers(options => options.Filters.Add<GlobalExceptionFilter>());

builder.Services.AddKafkaHandlers(Assembly.GetExecutingAssembly());
builder.Services.AddHostedService<KafkaConsumerService>();

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.AddRouting(options =>
{
    options.LowercaseUrls = true;
    options.LowercaseQueryStrings = true;
});

builder.Services.AddSwaggerGen(options =>
    options.SwaggerDoc("v1", new OpenApiInfo { Title = "My API", Version = "v1" }));

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "My API V1");
        c.RoutePrefix = string.Empty;
    });
}

app.UseHttpsRedirection();
app.MapControllers();

app.Run();