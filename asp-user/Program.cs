using asp_user.Contexts;
using asp_user.Extensions;
using Confluent.Kafka;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

builder.Configuration
    .AddJsonFile("appsettings.json", false, true)
    .AddJsonFile($"appsettings.{builder.Environment.EnvironmentName}.json", true)
    .AddEnvironmentVariables();

builder.WebHost.ConfigureKestrel(options => { options.ListenAnyIP(5232, o => o.Protocols = HttpProtocols.Http2); });

builder.Logging.ClearProviders();
builder.Logging.AddConsole();
builder.Services.AddOpenApi();
builder.Services.AddEndpointsApiExplorer();

builder.Services.Configure<ProducerConfig>(builder.Configuration.GetSection("Kafka:Producer"));
builder.Services.Configure<ConsumerConfig>(builder.Configuration.GetSection("Kafka:Consumer"));

// builder.Services.AddSingleton<KafkaProducerService>();
// builder.Services.AddKafkaHandlers(Assembly.GetExecutingAssembly());
// builder.Services.AddHostedService<KafkaConsumerService>();

builder.Services.AddGrpc(options =>
{
    options.MaxReceiveMessageSize = 50 * 1024 * 1024; // 50MB
    options.MaxSendMessageSize = 50 * 1024 * 1024; // 50MB
});

builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection"),
        npgsqlOptions => npgsqlOptions.MigrationsHistoryTable("__EFMigrationsHistory", "users")));

builder.Services.AddGrpcServices();

var app = builder.Build();

if (app.Environment.IsDevelopment()) app.MapOpenApi();

// app.MapControllers();
app.MapGrpcServices();

app.Run();