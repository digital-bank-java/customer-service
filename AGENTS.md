# AGENTS.md

## Repository Purpose

`customer-service` owns customer identity and profile management.

It is the source of truth for customer registration and customer profile state.

## Current Responsibilities

- register customer records
- retrieve a customer profile
- update a customer profile
- expose admin query APIs through the gateway
- persist customer data in PostgreSQL

## Current Non-Responsibilities

- account balances
- financial postings
- transaction orchestration
- direct secret management

## Architecture

- Hexagonal architecture
- Domain model under `domain`
- Use-case ports under `application.port.in`
- Outbound ports under `application.port.out`
- Web adapters under `adapter.in.web`
- Persistence adapters under `adapter.out.persistence`

## Key Commands

```bash
./mvnw test
./mvnw verify
./mvnw spring-boot:run
docker build -t digital-bank-java/customer-service:<tag> .
helm lint helm --strict
```

## Runtime and Data

- Default service port: `8081`
- Logical database in SIT: `customer_service`
- Runtime configuration comes from `config-repo`
- Credentials are injected through environment variables or Kubernetes secrets

## Testing Rules

- Use unit tests for domain/application behavior where isolation is enough.
- Use Testcontainers-backed integration tests for persistence and API slices.
- Keep `./mvnw verify` green before merge.

## Deployment Notes

- Public and admin access should flow through `api-gateway`.
- Port-forwarding the gateway is the normal local verification path.
- Port-forward the service directly only when debugging the service itself.

## Working Rules

- Keep customer state changes inside this service.
- Do not add account or ledger responsibilities here.
- Keep OpenAPI and admin query documentation aligned with actual gateway routes.
