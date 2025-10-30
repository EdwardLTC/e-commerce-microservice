using System.Reflection;
using asp_user.Consumers;
using asp_user.Contexts;
using asp_user.Extensions;
using asp_user.GrpcServiceClients;
using asp_user.Interceptors;
using asp_user.Kafka;
using Confluent.Kafka;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using Microsoft.EntityFrameworkCore;

WebApplicationBuilder builder = WebApplication.CreateBuilder(args);

builder.Configuration
	.AddJsonFile("appsettings.json", false, true)
	.AddJsonFile($"appsettings.{builder.Environment.EnvironmentName}.json", true)
	.AddEnvironmentVariables();

builder.WebHost.ConfigureKestrel(options => { options.ListenAnyIP(5232, o => o.Protocols = HttpProtocols.Http2); });

builder.Logging.ClearProviders();
builder.Logging.AddConsole();
builder.Services.AddOpenApi();
builder.Services.AddEndpointsApiExplorer();

builder.Services.AddSingleton(new OrderServiceClient(builder.Configuration.GetValue<string>("GrpcSettings:OrderServiceUrl") ?? "https://localhost:4040"));

builder.Services.Configure<ProducerConfig>(builder.Configuration.GetSection("Kafka:Producer"));
builder.Services.Configure<ConsumerConfig>(builder.Configuration.GetSection("Kafka:Consumer"));

builder.Services.AddTransient<UserConsumer>();
builder.Services.AddSingleton<KafkaProducerService>();
builder.Services.AddSingleton<KafkaAttributeConsumer>();
builder.Services.AddHostedService(sp => new BackgroundServiceRunner(sp.GetRequiredService<KafkaAttributeConsumer>()));

builder.Services.AddGrpc(options =>
{
	options.MaxReceiveMessageSize = 50 * 1024 * 1024; // 50MB
	options.MaxSendMessageSize = 50 * 1024 * 1024;    // 50MB
	options.Interceptors.Add<ValidationInterceptor>();
});

builder.Services.AddScoped<ValidationInterceptor>();

builder.Services.AddDbContext<AppDbContext>(options =>
	options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection"),
		npgsqlOptions => npgsqlOptions.MigrationsHistoryTable("__EFMigrationsHistory", "users")));

builder.Services.AddGrpcServices();
builder.Services.AddAttributedValidators(Assembly.GetExecutingAssembly());

WebApplication app = builder.Build();

if (app.Environment.IsDevelopment()) app.MapOpenApi();

app.MapGrpcServices();

app.Run();
