# Repository Guidelines

## Overview
- This repository contains a polyglot e-commerce microservice system.
- Main services:
  - `nest-api-gateway-auth`: NestJS API gateway and auth layer
  - `golang-order`: Go order service
  - `spring-boot-product`: Kotlin/Spring Boot product service
  - `asp-user`: ASP.NET user service
  - `kmm-app`: Kotlin Multiplatform client app
- Shared contracts live in `/.proto` and `/.avro`.

## Working Style
- Keep changes scoped to the service the task targets.
- Prefer fixing root causes over adding local workarounds.
- Do not refactor unrelated code while addressing a focused task.
- Preserve existing framework and language conventions in each service.
- Update documentation when behavior, setup, or developer workflow changes.

## Service Notes

### `nest-api-gateway-auth`
- Uses Node.js + NestJS.
- Common commands:
  - `npm install`
  - `npm run start:dev`
  - `npm run build`
  - `npm run test`
  - `npm run lint`
- Proto generation scripts exist in `package.json`; prefer existing scripts over ad hoc generation commands.

### `golang-order`
- Uses Go modules.
- Prefer standard Go tooling such as `go test ./...` and `go fmt ./...` when relevant.
- Be careful with generated or schema-driven code if the task touches gRPC or Avro integration.

### `spring-boot-product`
- Uses Gradle Kotlin DSL.
- Prefer the Gradle wrapper if present; otherwise use project-standard Gradle commands.
- Typical validation commands are `./gradlew test` and `./gradlew build` when available.
- This service consumes shared proto and avro definitions from the repository root.

### `asp-user`
- ASP.NET Core service.
- Follow existing .NET project structure and naming patterns in this directory.

### `kmm-app`
- Kotlin Multiplatform client application.
- Avoid changing mobile build configuration unless the task explicitly requires it.

## Shared Contract Rules
- Check `/.proto` and `/.avro` before changing inter-service request, response, or event formats.
- If a contract changes, update only the affected services and note regeneration needs.
- Prefer existing generation workflows already defined by each service.

## Validation
- Run the smallest relevant validation first for the service you changed.
- Broaden to cross-service or compose-level checks only when the task needs it.
- Do not attempt to fix unrelated failing tests.

## Containers
- Root orchestration is defined in `compose.yaml`.
- Some services also have local `docker-compose.yml` files for service-specific workflows.
- Prefer the smallest compose setup that verifies the task.

## Avoid
- Do not edit IDE, cache, or environment files unless the task explicitly requires it.
- Do not commit secrets into `.env` files.
- Do not hand-edit generated artifacts when the source contract or generator should be changed instead.
