# Customer Service Insomnia Requests

Use these requests under the existing `Customer Service` folder in the platform Insomnia collection.

## Environment Variables

The environment should include these variables:

```json
{
  "baseUrl": "http://localhost:8081",
  "configServerUrl": "http://localhost:8888",
  "customerId": ""
}
```

For `SIT` and `UAT`, replace `baseUrl` with the API Gateway or service URL for that environment.

## Register Customer

```text
POST {{ _.baseUrl }}/api/v1/customers
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

After a successful request, copy the `customerId` response value into the active Insomnia environment.

## Get Customer Profile

```text
GET {{ _.baseUrl }}/api/v1/customers/{{ _.customerId }}
```

Expected response:

```text
200 OK
```

## Update Customer Profile

```text
PATCH {{ _.baseUrl }}/api/v1/customers/{{ _.customerId }}/profile
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
GET {{ _.baseUrl }}/v3/api-docs
```

Expected response:

```text
200 OK
```

## Validation Failure Example

```text
POST {{ _.baseUrl }}/api/v1/customers
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
