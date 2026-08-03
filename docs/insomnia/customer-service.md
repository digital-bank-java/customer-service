# Customer Service Insomnia Requests

Use these requests under the existing `Customer Service` folder in the platform Insomnia collection.

## Environment Variables

Use the SIT API Gateway route for these requests. The environment should include:

```json
{
  "apiGatewayUrl": "http://localhost:8080"
}
```

For `SIT` and `UAT`, set `apiGatewayUrl` to the gateway URL for that environment.

## Register Customer

```text
POST {{ _.apiGatewayUrl }}/admin/v1/customers
```

Headers:

```text
Content-Type: application/json
```

Body:

```json
{
  "email": "customer@example.com",
  "mobileNumber": "+971501234567",
  "firstName": "Rami",
  "lastName": "Customer",
  "dateOfBirth": "1990-01-01"
}
```

Expected response:

```text
201 Created
```

After a successful request, use the returned `customerId` as the path parameter value for profile requests.

## Get Customer Profile

```text
GET {{ _.apiGatewayUrl }}/api/v1/customers/:customerId
```

Path parameters:

```text
customerId=<customerId returned by Register Customer>
```

Expected response:

```text
200 OK
```

## Update Customer Profile

```text
PATCH {{ _.apiGatewayUrl }}/api/v1/customers/:customerId/profile
```

Headers:

```text
Content-Type: application/json
```

Body:

```json
{
  "mobileNumber": "+971509999999",
  "firstName": "Updated",
  "lastName": "Customer",
  "expectedVersion": 0
}
```

Path parameters:

```text
customerId=<customerId returned by Register Customer>
```

Expected response:

```text
200 OK
```

If another update already changed the profile, the same request with an old `expectedVersion` returns:

```text
409 Conflict
```

## OpenAPI Contract

```text
GET {{ _.apiGatewayUrl }}/admin/docs/customer-service/v3/api-docs
```

Expected response:

```text
200 OK
```

## Validation Failure Example

```text
POST {{ _.apiGatewayUrl }}/admin/v1/customers
```

Body:

```json
{
  "email": "not-an-email",
  "mobileNumber": "0501234567",
  "firstName": "",
  "lastName": "Customer",
  "dateOfBirth": "2099-01-01"
}
```

Expected response:

```text
400 Bad Request
```

## Duplicate Customer Example

Run `Register Customer` twice with the same email.

Expected response for the second request:

```text
409 Conflict
```
