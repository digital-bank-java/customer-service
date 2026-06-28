# Customer Service

Customer profile and lifecycle service for the Digital Bank Java platform.

## Responsibilities

- Own customer identity and profile data within the banking domain.
- Manage customer lifecycle state and contact information.
- Expose customer capabilities to authorized platform services.
- Publish customer lifecycle events as event-driven capabilities are introduced.

## Non-Responsibilities

- Authentication, sessions, credentials, or MFA.
- Accounts, balances, transactions, or payment execution.
- Storing secrets or environment-specific configuration in the application image.

The current bootstrap establishes the deployable service boundary. Customer business APIs and persistence are introduced in subsequent stories.

## Architecture

The source tree follows hexagonal architecture boundaries:

```text
com.digitalbank.customerservice
|-- domain.model
|-- application.port.in
|-- application.port.out
|-- application.service
|-- adapter.in.web
|-- adapter.out.persistence
`-- configuration
```

The domain layer remains independent of Spring and persistence frameworks. Inbound adapters invoke application ports, while outbound adapters implement the persistence and integration contracts required by application services.

## Runtime Configuration

The service is a Spring Cloud Config client. It loads shared, service-specific, and environment-specific configuration from Config Server.

| Variable | Purpose | Default |
| --- | --- | --- |
| `CONFIG_SERVER_URL` | Config Server base URL | `http://localhost:8888` |
| `SPRING_PROFILES_ACTIVE` | Runtime environment profile | Spring `default` profile |

Secrets must not be committed to this repository or stored in the container image. Kubernetes and AWS environments will supply secrets through their approved secret-management integrations.

## Prerequisites

- Java 21.
- Network access to Maven Central for the initial dependency download.
- A running Config Server for normal application startup.
- Docker Desktop for image builds.
- Docker Desktop Kubernetes and Helm 4 for local SIT deployment.

A global Maven installation is not required because the Maven Wrapper is included.

```bash
java -version
./mvnw --version
docker version
kubectl config current-context
helm version --short
```

## Test

Run the complete Maven test suite from the repository root:

```bash
./mvnw test
```

Tests disable the external Config Server dependency so the build remains deterministic.

## Run Locally

Start Config Server on port `8888`, then start Customer Service with the local profile:

```bash
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

The configured service port is `8081`.

```bash
curl --fail http://localhost:8081/actuator/health
```

## Run With Docker

Build the image:

```bash
docker build \
  --tag digital-bank-java/customer-service:0.0.1 \
  .
```

On Docker Desktop, connect the container to Config Server running on the host:

```bash
docker run --rm \
  --name digital-bank-java-customer-service \
  --publish 8081:8081 \
  --env CONFIG_SERVER_URL=http://host.docker.internal:8888 \
  --env SPRING_PROFILES_ACTIVE=local \
  digital-bank-java/customer-service:0.0.1
```

The runtime image uses numeric non-root user and group `10001:10001`.

## Deploy To Local SIT

The Config Server release must already be healthy in the `digital-bank-sit` namespace. The Customer Service chart uses the internal Kubernetes address `http://config-server:8888` and activates the `sit` profile.

The shared local PostgreSQL release from `platform-infra-local` must also be installed in `digital-bank-sit`. Customer Service connects to the `customer_service` logical database through the in-cluster `postgres` Service and reads credentials from the existing `postgres` Kubernetes Secret.

Validate the chart without changing the cluster:

```bash
helm lint helm --values helm/values-sit.yaml

helm template customer-service helm --values helm/values-sit.yaml |
  kubectl apply --dry-run=client -f -
```

Install or upgrade the release:

```bash
helm upgrade --install customer-service helm \
  --namespace digital-bank-sit \
  --create-namespace \
  --values helm/values-sit.yaml \
  --wait \
  --timeout 5m
```

Inspect the deployment:

```bash
helm status customer-service --namespace digital-bank-sit
kubectl get deployment,pods,service --namespace digital-bank-sit
kubectl logs deployment/customer-service --namespace digital-bank-sit
```

Temporarily forward the internal Service for workstation verification:

```bash
kubectl port-forward \
  service/customer-service 18081:8081 \
  --namespace digital-bank-sit
```

From another terminal:

```bash
curl --fail http://localhost:18081/actuator/health
curl --fail http://localhost:18081/actuator/health/liveness
curl --fail http://localhost:18081/actuator/health/readiness
```

After API Gateway is deployed, prefer verifying Customer Service through the gateway:

```bash
kubectl port-forward \
  service/api-gateway 8080:8080 \
  --namespace digital-bank-sit
```

In Insomnia, create equivalent requests using the environment variable:

```text
GET {{ _.apiGatewayUrl }}/customer-service/actuator/health
GET {{ _.apiGatewayUrl }}/admin/docs/customer-service/v3/api-docs
POST {{ _.apiGatewayUrl }}/api/v1/customers
GET {{ _.apiGatewayUrl }}/api/v1/customers/{{ _.customerId }}
PATCH {{ _.apiGatewayUrl }}/api/v1/customers/{{ _.customerId }}/profile
GET {{ _.apiGatewayUrl }}/admin/v1/customers?page=0&size=20
GET {{ _.apiGatewayUrl }}/admin/v1/customers?status=ACTIVE&page=0&size=20&sort=createdAt,desc
GET {{ _.apiGatewayUrl }}/admin/v1/customers?email={{ _.customerEmail }}&page=0&size=20&sort=email,asc
```

Use this request body when registering a customer:

```json
{
  "email": "customer@example.com",
  "mobileNumber": "+971501234567",
  "firstName": "Rami",
  "lastName": "Customer",
  "dateOfBirth": "1990-01-01"
}
```

The equivalent terminal command is:

```bash
curl --request POST http://localhost:8080/api/v1/customers \
  --header "Content-Type: application/json" \
  --data '{
    "email": "customer@example.com",
    "mobileNumber": "+971501234567",
    "firstName": "Rami",
    "lastName": "Customer",
    "dateOfBirth": "1990-01-01"
  }'
```

Copy the returned `customerId`, then verify lookup and admin query endpoints:

```bash
curl --fail http://localhost:8080/api/v1/customers/<customer-id>
curl --fail "http://localhost:8080/admin/v1/customers?status=ACTIVE&page=0&size=20&sort=createdAt,desc"
curl --fail "http://localhost:8080/admin/v1/customers?email=customer@example.com&page=0&size=20&sort=email,asc"
```

Stop port forwarding with `Ctrl+C`. Remove only this release when cleanup is required:

```bash
helm uninstall customer-service --namespace digital-bank-sit
```

## Deployment Security

The Kubernetes deployment:

- Runs as numeric non-root user and group `10001`.
- Disables privilege escalation and drops Linux capabilities.
- Uses a read-only root filesystem with bounded temporary storage.
- Does not mount the default Kubernetes service account token.
- Exposes the application only through an internal `ClusterIP` Service.
- Defines startup, liveness, and readiness probes.

## CI Validation

Pull requests and changes to `main` run independent jobs that:

- Execute Maven verification with Java 21.
- Lint and render the Helm chart with Helm 4.2.0.
- Build the container image, verify its non-root user, and smoke-test its health endpoint.

Third-party GitHub Actions are pinned to immutable commit SHAs.

## Environment Promotion

The same application artifact is intended to move through SIT, UAT, and PROD without being rebuilt. Deployment pipelines provide environment-specific immutable image tags, Config Server addresses, profiles, resource sizing, and infrastructure integrations.

AWS deployment will map the Kubernetes workload to EKS and use managed AWS services for configuration credentials, networking, observability, and secrets. Environment-specific secrets remain outside Git and Helm values.

## Development Workflow

Changes must be made on a dedicated branch and merged through a pull request. Do not commit directly to `main`.

Before opening a pull request:

```bash
git status
./mvnw test
helm lint helm --strict
git diff --check
```
