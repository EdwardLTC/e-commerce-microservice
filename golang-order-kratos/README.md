# golang-order-kratos

This folder contains a Kratos-based rewrite of the `golang-order` service.

## Scope

- Owns its generated protobuf package locally in `gen/proto`, generated from the shared `../.proto` definitions
- Owns its Ent schema and generated package locally in `schema` and `gen/ent`
- Owns its Avro generated package locally in `gen/avro`, generated from the shared `../.avro` schema files
- Restructures the service into Kratos-style layers:
  - `cmd/` bootstrap
  - `internal/service/` transport handlers
  - `internal/biz/` use cases
  - `internal/data/` repositories and clients
  - `internal/server/` Kratos transport setup
  - `internal/worker/` outbox and Kafka background processing

## Environment

Expected environment variables:

- `APP_PORT` default `2002`
- `DATABASE_URL`
- `PRODUCT_SERVICE_URL`
- `KAFKA_BROKER`
- `KAFKA_CONSUMER_GROUP` default `order-service-group`
- `OUTBOX_INTERVAL` default `5s`
- `OUTBOX_BATCH` default `20`

## Notes

- Regenerate local protobuf, Avro, and Ent code from this folder with `go generate ./...`.
- The `ListOrdersRequest.status` field is still ambiguous for the `CREATED` enum value because the current protobuf contract uses a non-optional enum. This rewrite preserves that behavior instead of changing the shared contract.
- This folder is intentionally standalone so the existing `golang-order` service can remain the active implementation while the Kratos version is completed and validated.
