# E-Commerce Microservice Project Presentation

## 1. Project Summary

This project is a polyglot e-commerce platform built with a microservice architecture. The main goal was to design a system where each business domain can evolve independently while still working together in a consistent order-processing flow.

Instead of building the whole platform in one stack, I split responsibilities across multiple services and used the communication style that best fits each use case:

- HTTP for client-to-gateway access
- gRPC for synchronous internal service calls
- Kafka for asynchronous domain events
- Redis for authentication/session-related data
- PostgreSQL as the primary persistence layer for each service

The project is not just CRUD split into multiple repos. The core engineering focus is distributed transaction handling for order creation using the Saga pattern with choreography.

## 2. What Problem This Project Solves

In a real e-commerce system, creating an order is not a single database transaction:

- the order must be created
- stock must be reserved
- the customer balance or payment must be processed
- failures must be handled without leaving inconsistent state

This project models that workflow across independent services. The design shows how to keep services decoupled while still reaching eventual consistency.

## 3. Architecture Overview

### Main Components

#### API Gateway
- Technology: NestJS
- Role: entry point for clients
- Responsibilities:
  - exposes HTTP APIs
  - handles authentication
  - applies global guard, interceptor, and exception handling
  - forwards requests to backend services through gRPC
  - exposes Swagger docs in development at `/docs`

#### User Service
- Technology: ASP.NET Core with gRPC
- Role: user domain and wallet/payment-related actions
- Responsibilities:
  - user registration and account management
  - balance deduction during the order flow
  - Kafka event consumption and publishing
  - outbox-based event dispatch support

#### Product Service
- Technology: Spring Boot with Kotlin
- Role: product catalog and inventory domain
- Responsibilities:
  - product, option type, option value, and variant management
  - stock reservation after an order is created
  - stock release when payment fails
  - Kafka-based inventory event handling

#### Order Service
- Technology: Go with gRPC
- Role: order lifecycle orchestration through choreography
- Responsibilities:
  - create orders
  - validate variants and pricing before order creation
  - persist order data and order items
  - publish `order.created` through an outbox table
  - update order state based on downstream events

#### Mobile Client
- Technology: Kotlin Multiplatform + Compose Multiplatform
- Role: cross-platform client prototype
- Responsibilities:
  - login flow
  - basic authenticated app entry
  - home/profile/settings structure for Android and iOS

### Communication Model

- Client to gateway: HTTP
- Gateway to services: gRPC
- Service to service async flow: Kafka events

This split makes sense because request/response operations need low latency, while order processing across services needs reliable asynchronous communication.

## 4. Key Technical Design

### Polyglot Microservices

I intentionally used different stacks for different services:

- NestJS for fast API gateway development
- Go for a lightweight order service
- Spring Boot Kotlin for product and inventory logic
- ASP.NET Core for user and wallet management

This demonstrates that the system contract matters more than a single language. gRPC and Avro schemas provide the shared boundary.

### Saga Pattern With Choreography

The most important part of the project is the order flow:

1. The API Gateway receives the order request.
2. The Order Service validates variants through gRPC and creates the order locally.
3. The Order Service stores an `order.created` event in its outbox.
4. The outbox dispatcher publishes that event to Kafka.
5. The Product Service consumes the event and reserves stock.
6. If stock reservation succeeds, Product publishes `stock.reduction.success`.
7. The User Service consumes that event and decreases the user wallet.
8. If payment succeeds, the process continues to completion.
9. If payment fails, Product releases stock and Order updates the order to a failure state.

Why this matters:

- there is no fragile distributed database transaction
- each service owns its own data
- failures trigger compensating actions instead of partial corruption
- the system is closer to how production commerce platforms behave

### Transaction Outbox Pattern

The Order Service and User Service both contain outbox-related logic. This is important because publishing to Kafka should only happen after the local database transaction is safely committed.

That pattern reduces the risk of:

- writing business data without emitting the event
- emitting the event without committing the business data

For interviewer discussion, this is one of the strongest architecture decisions in the project.

## 5. Order Flow To Present

When presenting the project, this is the clearest business story:

### Happy Path

- A client places an order through the API Gateway.
- The Order Service creates the order and saves order items.
- Product reserves inventory.
- User deducts wallet balance.
- The order reaches a completed or payment-completed state.

### Failure Handling

- If stock is unavailable, Product emits `stock.reduction.fail`.
- If payment fails, User emits `payment.fail`.
- Product compensates by releasing stock.
- Order keeps the final failure state for visibility and recovery.

This is a good place to explain eventual consistency and why rollback in distributed systems is handled by business events instead of database transactions.

## 6. What I Would Highlight In An Interview

### Engineering Strengths

- Designed a real distributed workflow instead of isolated CRUD services
- Used gRPC for strong internal contracts and better service-to-service performance
- Applied Kafka for event-driven communication
- Implemented Saga choreography for distributed transaction management
- Used the transaction outbox pattern for safer event publishing
- Separated data ownership by service
- Added an API gateway with centralized authentication and documentation
- Built a companion mobile app with Kotlin Multiplatform to show end-to-end thinking

### Tradeoffs I Would Explain Honestly

- Choreography improves decoupling, but tracing failures across services becomes harder
- Polyglot services are powerful, but they increase operational complexity
- This repo shows strong architecture and integration patterns, but production readiness would still require more work in observability, testing depth, CI/CD, and deployment automation

Being clear about tradeoffs usually makes the presentation stronger.

## 7. Interviewer-Friendly Tech Stack Summary

| Area | Technology |
| --- | --- |
| API Gateway | NestJS |
| Order Service | Go + gRPC + Ent |
| Product Service | Spring Boot + Kotlin + Kafka |
| User Service | ASP.NET Core + gRPC + EF Core + Kafka |
| Client App | Kotlin Multiplatform + Compose |
| Message Broker | Kafka |
| Cache | Redis |
| Database | PostgreSQL |
| API Contract | gRPC + Protobuf |
| Event Contract | Avro |

## 8. Suggested Demo Script

If I had 3 to 5 minutes to present this project, I would say:

> This is an e-commerce microservice system I built to explore distributed transaction design in a realistic business flow. The platform has a NestJS API Gateway, a Go Order Service, a Kotlin Spring Boot Product Service, and an ASP.NET Core User Service. Services communicate synchronously with gRPC and asynchronously with Kafka.
>
> The core feature is order processing with the Saga pattern using choreography. When an order is created, the Order Service stores an outbox event, Product reserves inventory, User processes wallet deduction, and failures trigger compensating actions like releasing stock. That lets each service keep ownership of its own database while still achieving eventual consistency.
>
> The main thing I wanted to prove with this project was not just that I can create APIs, but that I can reason about service boundaries, contracts, asynchronous workflows, and failure recovery in a distributed system.

## 9. Good Questions To Expect

You should be ready to answer:

- Why did you choose choreography instead of orchestration?
- Why gRPC internally instead of REST between services?
- How do you prevent duplicate event processing?
- How does the outbox pattern improve reliability?
- What happens if Kafka is down after the database commit?
- What would you add next for production readiness?
- How would you monitor or trace a failed order across services?

## 10. Suggested Answers For Interview Questions

### Why did you choose choreography instead of orchestration?

I chose choreography because I wanted each service to stay autonomous and react to business events instead of depending on a central coordinator. In this project, the order flow is naturally event-driven: Order emits `order.created`, Product reacts by reserving stock, and User reacts by processing payment. That keeps coupling lower and makes it easier to extend the workflow later by adding new subscribers.

The tradeoff is that choreography is harder to observe and debug than orchestration, because the flow is distributed across services. For this project, that tradeoff was acceptable because the goal was to demonstrate distributed transaction design and service independence.

### Why gRPC internally instead of REST between services?

I used gRPC internally because service-to-service communication benefits from stronger contracts and better efficiency than plain REST. With gRPC, I get:

- strongly typed interfaces through Protobuf
- lower serialization overhead
- better performance for internal request/response calls
- clearer service boundaries across different languages

REST is still a good fit for external clients, which is why the API Gateway exposes HTTP. But inside the system, gRPC is a better choice for controlled, high-frequency service communication.

### How do you prevent duplicate event processing?

The main strategy is idempotent consumers. In an event-driven system, duplicate delivery can happen, so consumers should be safe to run more than once for the same business event.

In practice, I would handle that by:

- storing a processed event ID or message key
- checking current business state before applying updates
- making stock release, payment failure handling, and status updates idempotent
- using the order ID as the correlation key across the saga

The architecture already points in that direction, and the next production step would be to enforce idempotency explicitly across every consumer.

### How does the outbox pattern improve reliability?

The outbox pattern solves a classic consistency problem: a service updates its database and also needs to publish an event. If those two actions are separate, one can succeed while the other fails.

With the outbox pattern:

- the business data and event record are written in the same local transaction
- a dispatcher publishes pending outbox events afterward
- if publishing fails, the event remains in the database and can be retried

That gives a much safer guarantee than trying to update the database and publish to Kafka independently.

### What happens if Kafka is down after the database commit?

If Kafka is down after the database commit, the business transaction is still preserved because the event was already written to the outbox table as part of that commit. The dispatcher will fail to publish temporarily, but the event is not lost.

Once Kafka becomes available again, the dispatcher can retry and publish the pending event. This is exactly why the outbox pattern is useful: it turns a risky dual-write problem into a retryable background delivery problem.

### What would you add next for production readiness?

The next production-focused improvements would be:

- distributed tracing across gateway and services
- centralized structured logging
- stronger idempotency enforcement for all event consumers
- dead-letter topics and clearer retry policies
- integration and contract tests across services
- CI/CD pipelines and container deployment manifests
- health checks, alerts, and service-level dashboards
- a unified local and staging environment setup

The architecture direction is strong, but production readiness depends on observability, automation, and operational hardening.

### How would you monitor or trace a failed order across services?

I would trace failures by combining correlation IDs, structured logs, metrics, and distributed tracing.

Concretely:

- use `orderId` as the correlation ID across all events and service logs
- emit structured logs at every state transition
- expose metrics for failed events, retries, outbox backlog, and consumer lag
- add OpenTelemetry tracing from the API Gateway through gRPC calls and Kafka handlers
- create dashboards showing the order lifecycle and failure counts by stage

That would let me answer questions like:

- Did the order get created?
- Did Product receive `order.created`?
- Did stock reservation fail?
- Did User publish `payment.fail`?
- Is the message stuck in the outbox or consumer retry loop?

## 11. Practical Next Improvements

If an interviewer asks what you would do next, strong answers are:

- add distributed tracing and centralized logging
- improve integration and contract testing across services
- add retry policies, dead-letter handling, and idempotency guarantees everywhere
- unify local development with one complete Docker Compose setup
- add CI/CD pipelines and container deployment manifests
- extend the mobile app to cover the full shopping and checkout flow

## 12. Repository References

- Main architecture overview: [README.md](/Users/edward/Documents/e-commerce-microservice/README.md)
- API Gateway bootstrap: [main.ts](/Users/edward/Documents/e-commerce-microservice/nest-api-gateway-auth/src/main.ts)
- Gateway module wiring: [app.module.ts](/Users/edward/Documents/e-commerce-microservice/nest-api-gateway-auth/src/app/app.module.ts)
- Order creation and outbox write: [choreography.go](/Users/edward/Documents/e-commerce-microservice/golang-order/internal/order/choreography.go)
- Order outbox dispatcher: [dispatcher.go](/Users/edward/Documents/e-commerce-microservice/golang-order/internal/outbox/dispatcher.go)
- Product stock event consumer: [StockConsumer.kt](/Users/edward/Documents/e-commerce-microservice/spring-boot-product/src/main/kotlin/com/ecommerce/springboot/product/consumers/StockConsumer.kt)
- User payment event consumer: [UserConsumer.cs](/Users/edward/Documents/e-commerce-microservice/asp-user/Consumers/UserConsumer.cs)
- System diagram asset: [system overview.png](/Users/edward/Documents/e-commerce-microservice/public/system%20overview.png)

## 13. Final Positioning

The best way to present this project is as a distributed systems project with an e-commerce use case, not just as a shopping app. The strongest signals are service boundaries, gRPC contracts, Kafka-driven saga flow, and the outbox pattern for reliable event publication.
