package main

import (
	"context"
	"os"

	"github.com/joho/godotenv"

	kratos "github.com/go-kratos/kratos/v2"
	klog "github.com/go-kratos/kratos/v2/log"

	"golang-order-kratos/internal/biz"
	"golang-order-kratos/internal/conf"
	"golang-order-kratos/internal/data"
	"golang-order-kratos/internal/server"
	"golang-order-kratos/internal/service"
	"golang-order-kratos/internal/worker"
)

func main() {
	_ = godotenv.Load()

	cfg := conf.FromEnv()
	baseLogger := klog.NewStdLogger(os.Stdout)
	logger := klog.NewHelper(baseLogger)

	entClient, err := data.NewEntClient(cfg.Data.DatabaseURL)
	if err != nil {
		logger.Fatalf("failed to open database: %v", err)
	}
	defer func() {
		if err := entClient.Close(); err != nil {
			logger.Errorf("failed to close database: %v", err)
		}
	}()

	variantCatalog, err := data.NewVariantCatalog(cfg.Clients.ProductServiceURL)
	if err != nil {
		logger.Fatalf("failed to connect product service: %v", err)
	}
	defer func() {
		if err := variantCatalog.Close(); err != nil {
			logger.Errorf("failed to close product client: %v", err)
		}
	}()

	orderRepo := data.NewOrderRepo(entClient)
	usecase := biz.NewOrderUsecase(orderRepo, variantCatalog, baseLogger)
	orderService := service.NewOrderService(usecase)
	grpcServer := server.NewGRPCServer(cfg.Server.GRPCAddr, orderService, baseLogger)

	producer, err := data.NewKafkaProducer(cfg.Kafka.Broker)
	if err != nil {
		logger.Fatalf("failed to create kafka producer: %v", err)
	}
	defer producer.Close()

	dispatcher := worker.NewOutboxDispatcher(orderRepo, producer, cfg.Worker.OutboxInterval, cfg.Worker.OutboxBatch, baseLogger)
	consumer, err := worker.NewOrderEventConsumer(cfg.Kafka.Broker, cfg.Kafka.ConsumerGroup, cfg.Worker.Topics, usecase, baseLogger)
	if err != nil {
		logger.Fatalf("failed to create kafka consumer: %v", err)
	}

	workerCtx, cancelWorkers := context.WithCancel(context.Background())
	defer cancelWorkers()

	go dispatcher.Start(workerCtx)
	go consumer.Start(workerCtx)

	app := kratos.New(
		kratos.Name("golang-order-kratos"),
		kratos.Logger(baseLogger),
		kratos.Server(grpcServer),
	)

	if err := app.Run(); err != nil {
		logger.Fatalf("kratos app stopped with error: %v", err)
	}

	cancelWorkers()
	if err := consumer.Stop(); err != nil {
		logger.Errorf("failed to stop kafka consumer: %v", err)
	}
}
