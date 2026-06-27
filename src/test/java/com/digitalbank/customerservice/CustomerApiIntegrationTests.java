package com.digitalbank.customerservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerApiIntegrationTests {

	@Container
	private static final PostgreSQLContainer postgres = new PostgreSQLContainer(
			DockerImageName.parse("postgres:16-alpine"));

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@LocalServerPort
	private int port;

	@DynamicPropertySource
	static void configureDatasource(DynamicPropertyRegistry registry) {
		registry.add("spring.cloud.config.enabled", () -> "false");
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
		registry.add("spring.jpa.open-in-view", () -> "false");
	}

	@Test
	void registersAndRetrievesCustomer() throws Exception {
		var registrationResponse = sendJson("POST", "/api/v1/customers", """
				{
				  "email": "Api.Customer@example.com",
				  "mobileNumber": "+971501111111",
				  "firstName": "Api",
				  "lastName": "Customer",
				  "dateOfBirth": "1990-01-01"
				}
				""");

		assertThat(registrationResponse.statusCode()).isEqualTo(201);
		assertThat(registrationResponse.headers().firstValue("location")).hasValueSatisfying(location -> {
			assertThat(location).startsWith("/api/v1/customers/");
		});

		var createdCustomer = objectMapper.readTree(registrationResponse.body());
		assertThat(createdCustomer.path("customerId").asText()).isNotBlank();
		assertThat(createdCustomer.path("email").asText()).isEqualTo("api.customer@example.com");
		assertThat(createdCustomer.path("status").asText()).isEqualTo("ACTIVE");

		var profileResponse = send("GET", "/api/v1/customers/" + createdCustomer.path("customerId").asText());

		assertThat(profileResponse.statusCode()).isEqualTo(200);
		var profile = objectMapper.readTree(profileResponse.body());
		assertThat(profile.path("customerId").asText()).isEqualTo(createdCustomer.path("customerId").asText());
		assertThat(profile.path("email").asText()).isEqualTo("api.customer@example.com");
	}

	@Test
	void updatesCustomerProfile() throws Exception {
		var registrationResponse = sendJson("POST", "/api/v1/customers", """
				{
				  "email": "profile.update@example.com",
				  "mobileNumber": "+971504444444",
				  "firstName": "Profile",
				  "lastName": "Customer",
				  "dateOfBirth": "1990-01-01"
				}
				""");
		var createdCustomer = objectMapper.readTree(registrationResponse.body());

		var updateResponse = sendJson("PATCH", "/api/v1/customers/" + createdCustomer.path("customerId").asText() + "/profile", """
				{
				  "mobileNumber": "+971505555555",
				  "firstName": "Updated",
				  "lastName": "Customer",
				  "expectedVersion": 0
				}
				""");

		assertThat(updateResponse.statusCode()).isEqualTo(200);
		var updatedCustomer = objectMapper.readTree(updateResponse.body());
		assertThat(updatedCustomer.path("mobileNumber").asText()).isEqualTo("+971505555555");
		assertThat(updatedCustomer.path("firstName").asText()).isEqualTo("Updated");
		assertThat(updatedCustomer.path("version").asLong()).isEqualTo(1L);
	}

	@Test
	void rejectsStaleProfileUpdate() throws Exception {
		var registrationResponse = sendJson("POST", "/api/v1/customers", """
				{
				  "email": "stale.update@example.com",
				  "mobileNumber": "+971506666666",
				  "firstName": "Stale",
				  "lastName": "Customer",
				  "dateOfBirth": "1990-01-01"
				}
				""");
		var createdCustomer = objectMapper.readTree(registrationResponse.body());

		var updateResponse = sendJson("PATCH", "/api/v1/customers/" + createdCustomer.path("customerId").asText() + "/profile", """
				{
				  "mobileNumber": "+971507777777",
				  "firstName": "Updated",
				  "lastName": "Customer",
				  "expectedVersion": 99
				}
				""");

		assertThat(updateResponse.statusCode()).isEqualTo(409);
		var problem = objectMapper.readTree(updateResponse.body());
		assertThat(problem.path("title").asText()).isEqualTo("Customer profile conflict");
		assertThat(problem.path("currentVersion").asLong()).isEqualTo(0L);
		assertThat(problem.path("expectedVersion").asLong()).isEqualTo(99L);
	}

	@Test
	void rejectsDuplicateCustomerEmail() throws Exception {
		sendJson("POST", "/api/v1/customers", """
				{
				  "email": "duplicate@example.com",
				  "mobileNumber": "+971502222222",
				  "firstName": "Duplicate",
				  "lastName": "Customer",
				  "dateOfBirth": "1990-01-01"
				}
				""");

		var duplicateResponse = sendJson("POST", "/api/v1/customers", """
				{
				  "email": "DUPLICATE@example.com",
				  "mobileNumber": "+971503333333",
				  "firstName": "Duplicate",
				  "lastName": "Customer",
				  "dateOfBirth": "1990-01-01"
				}
				""");

		assertThat(duplicateResponse.statusCode()).isEqualTo(409);
		var problem = objectMapper.readTree(duplicateResponse.body());
		assertThat(problem.path("title").asText()).isEqualTo("Customer already exists");
		assertThat(problem.path("field").asText()).isEqualTo("email");
	}

	@Test
	void rejectsInvalidRegistrationRequest() throws Exception {
		var response = sendJson("POST", "/api/v1/customers", """
				{
				  "email": "not-an-email",
				  "mobileNumber": "0501234567",
				  "firstName": "",
				  "lastName": "Customer",
				  "dateOfBirth": "2099-01-01"
				}
				""");

		assertThat(response.statusCode()).isEqualTo(400);
		var problem = objectMapper.readTree(response.body());
		assertThat(problem.path("title").asText()).isEqualTo("Invalid request");
		assertThat(problem.path("errors")).isNotEmpty();
	}

	@Test
	void publishesOpenApiContract() throws Exception {
		var response = send("GET", "/v3/api-docs");

		assertThat(response.statusCode()).isEqualTo(200);
		var openApi = objectMapper.readTree(response.body());
		assertThat(openApi.path("paths").has("/api/v1/customers")).isTrue();

		var registerResponses = openApi.path("paths").path("/api/v1/customers").path("post").path("responses");
		assertThat(registerResponses.path("201").path("content").has("application/json")).isTrue();
		assertThat(registerResponses.path("400").path("content").has("application/problem+json")).isTrue();
		assertThat(registerResponses.path("409").path("content").has("application/problem+json")).isTrue();
		assertThat(registerResponses.path("400").path("content").has("application/json")).isFalse();

		var getProfileResponses = openApi.path("paths").path("/api/v1/customers/{customerId}").path("get").path("responses");
		assertThat(getProfileResponses.path("200").path("content").has("application/json")).isTrue();
		assertThat(getProfileResponses.path("404").path("content").has("application/problem+json")).isTrue();

		var updateProfileResponses = openApi.path("paths").path("/api/v1/customers/{customerId}/profile").path("patch").path("responses");
		assertThat(updateProfileResponses.path("200").path("content").has("application/json")).isTrue();
		assertThat(updateProfileResponses.path("400").path("content").has("application/problem+json")).isTrue();
		assertThat(updateProfileResponses.path("404").path("content").has("application/problem+json")).isTrue();
		assertThat(updateProfileResponses.path("409").path("content").has("application/problem+json")).isTrue();
	}

	private HttpResponse<String> send(String method, String path) throws Exception {
		var request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + path))
				.method(method, HttpRequest.BodyPublishers.noBody())
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> sendJson(String method, String path, String body) throws Exception {
		var request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.method(method, HttpRequest.BodyPublishers.ofString(body))
				.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}
}
