package main

//go:generate sh -c "protoc --proto_path=../.proto --go_out=paths=source_relative:./gen --go-grpc_out=paths=source_relative:./gen ../.proto/*.proto"
