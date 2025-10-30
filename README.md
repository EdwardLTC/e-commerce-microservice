# Untitled

# Microservice Architecture Documentation

## System Overview

This project implements a microservice architecture pattern with multiple services communicating through gRPC and Kafka message broker. The system follows a distributed approach with an API Gateway handling client requests and forwarding them to specialized microservices.

## Architecture Components

### API Gateway & Authentication

- **Technology:** NestJS
- **Responsibilities:**
    - Receives HTTP requests from clients
    - Handles authentication and authorization
    - Routes requests to appropriate microservices via gRPC
    - Stores authentication information in Redis
- **Cache Layer:** Redis for authentication tokens and session management

### Microservices

### Order Service

- **Technology:** Golang
- **Database:** PostgreSQL
- **Responsibilities:** Manages order creation, processing, and lifecycle

### Product Service

- **Technology:** Spring Boot (Kotlin)
- **Database:** PostgreSQL
- **Responsibilities:** Handles product catalog, inventory, and product information

### User Service

- **Technology:** [ASP.NET](http://ASP.NET) Core (C#)
- **Database:** PostgreSQL
- **Responsibilities:** Manages user profiles, preferences, and user-related operations

## Communication Patterns

### Synchronous Communication

- **Protocol:** gRPC
- **Usage:** Real-time request-response communication between API Gateway and microservices
- **Benefits:** High performance, type-safe, efficient binary protocol

### Asynchronous Communication

- **Message Broker:** Apache Kafka
- **Usage:** Event-driven communication for asynchronous operations
- **Pattern:** Saga pattern with choreography-based approach

## Saga Pattern Implementation

The system implements the Saga pattern with choreography-based coordination to maintain data consistency across distributed transactions.

### Choreography-Based Saga

- Each service listens to domain events and decides when to act
- Services publish events to Kafka topics when local transactions complete
- Other services subscribe to relevant events and execute their part of the saga
- Compensating transactions are triggered automatically on failures

## Data Storage

- **Database:** PostgreSQL for all microservices
- **Database per Service:** Each microservice maintains its own database instance
- **Cache:** Redis for authentication data and session management

## Technology Stack Summary

| **Component** | **Technology** | **Purpose** |
| --- | --- | --- |
| API Gateway | NestJS | Request routing & authentication |
| Order Service | Golang | Order management |
| Product Service | Spring Boot | Product catalog |
| User Service | ASP.NET Core | User management |
| Cache | Redis | Authentication & sessions |
| Database | PostgreSQL | Persistent storage |
| Message Broker | Apache Kafka | Event streaming & async messaging |
| RPC Protocol | gRPC | Inter-service communication |

## Key Architectural Benefits

- **Polyglot Architecture:** Each service uses the most appropriate technology for its domain
- **Scalability:** Services can be scaled independently based on demand
- **Resilience:** Saga pattern ensures eventual consistency and handles distributed transaction failures
- **Performance:** gRPC provides efficient synchronous communication, while Kafka handles asynchronous events
- **Maintainability:** Clear separation of concerns with dedicated services for each business domain


## System overview Diagram
![system overview.png](public/system%20overview.png)
