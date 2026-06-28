package com.digitalbank.customerservice.adapter.in.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.digitalbank.customerservice.application.model.CustomerSortOrder;
import com.digitalbank.customerservice.application.port.in.GetCustomerProfileUseCase;
import com.digitalbank.customerservice.application.port.in.ListCustomersQuery;
import com.digitalbank.customerservice.application.port.in.ListCustomersUseCase;
import com.digitalbank.customerservice.application.port.in.RegisterCustomerCommand;
import com.digitalbank.customerservice.application.port.in.RegisterCustomerUseCase;
import com.digitalbank.customerservice.application.port.in.UpdateCustomerProfileCommand;
import com.digitalbank.customerservice.application.port.in.UpdateCustomerProfileUseCase;
import com.digitalbank.customerservice.domain.model.CustomerId;
import com.digitalbank.customerservice.domain.model.CustomerStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Tag(name = "Customers")
@Validated
class CustomerController {

	private final RegisterCustomerUseCase registerCustomerUseCase;
	private final GetCustomerProfileUseCase getCustomerProfileUseCase;
	private final UpdateCustomerProfileUseCase updateCustomerProfileUseCase;
	private final ListCustomersUseCase listCustomersUseCase;

	CustomerController(
			RegisterCustomerUseCase registerCustomerUseCase,
			GetCustomerProfileUseCase getCustomerProfileUseCase,
			UpdateCustomerProfileUseCase updateCustomerProfileUseCase,
			ListCustomersUseCase listCustomersUseCase) {
		this.registerCustomerUseCase = registerCustomerUseCase;
		this.getCustomerProfileUseCase = getCustomerProfileUseCase;
		this.updateCustomerProfileUseCase = updateCustomerProfileUseCase;
		this.listCustomersUseCase = listCustomersUseCase;
	}

	@PostMapping("/api/v1/customers")
	@Operation(summary = "Register a customer")
	@ApiResponse(responseCode = "201", description = "Customer registered", content = @Content(
			mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = CustomerResponse.class)))
	@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(
			mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			schema = @Schema(implementation = ProblemDetail.class),
			examples = @ExampleObject(
					name = "validation-error",
					summary = "Validation failure",
					value = VALIDATION_PROBLEM_EXAMPLE)))
	@ApiResponse(responseCode = "409", description = "Customer already exists", content = @Content(
			mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			schema = @Schema(implementation = ProblemDetail.class),
			examples = @ExampleObject(
					name = "customer-conflict",
					summary = "Duplicate customer",
					value = CUSTOMER_CONFLICT_PROBLEM_EXAMPLE)))
	ResponseEntity<CustomerResponse> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
		var profile = registerCustomerUseCase.registerCustomer(new RegisterCustomerCommand(
				request.email(),
				request.mobileNumber(),
				request.firstName(),
				request.lastName(),
				request.dateOfBirth()));
		var response = CustomerResponse.from(profile);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.location(URI.create("/api/v1/customers/" + response.customerId()))
				.body(response);
	}

	@GetMapping("/api/v1/customers/{customerId}")
	@Operation(summary = "Get a customer profile")
	@ApiResponse(responseCode = "200", description = "Customer profile returned", content = @Content(
			mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = CustomerResponse.class)))
	@ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(
			mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			schema = @Schema(implementation = ProblemDetail.class),
			examples = @ExampleObject(
					name = "customer-not-found",
					summary = "Customer not found",
					value = CUSTOMER_NOT_FOUND_PROBLEM_EXAMPLE)))
	ResponseEntity<CustomerResponse> getCustomerProfile(@PathVariable UUID customerId) {
		var profile = getCustomerProfileUseCase.getCustomerProfile(new CustomerId(customerId));
		return ResponseEntity.ok(CustomerResponse.from(profile));
	}

	@PatchMapping("/api/v1/customers/{customerId}/profile")
	@Operation(summary = "Update customer profile")
	@ApiResponse(responseCode = "200", description = "Customer profile updated", content = @Content(
			mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = CustomerResponse.class)))
	@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(
			mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			schema = @Schema(implementation = ProblemDetail.class),
			examples = @ExampleObject(
					name = "validation-error",
					summary = "Validation failure",
					value = VALIDATION_PROBLEM_EXAMPLE)))
	@ApiResponse(responseCode = "404", description = "Customer not found", content = @Content(
			mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			schema = @Schema(implementation = ProblemDetail.class),
			examples = @ExampleObject(
					name = "customer-not-found",
					summary = "Customer not found",
					value = CUSTOMER_NOT_FOUND_PROBLEM_EXAMPLE)))
	@ApiResponse(responseCode = "409", description = "Duplicate or stale update", content = @Content(
			mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			schema = @Schema(implementation = ProblemDetail.class),
			examples = @ExampleObject(
					name = "profile-conflict",
					summary = "Stale profile update",
					value = PROFILE_CONFLICT_PROBLEM_EXAMPLE)))
	ResponseEntity<CustomerResponse> updateCustomerProfile(
			@PathVariable UUID customerId,
			@Valid @RequestBody UpdateCustomerProfileRequest request) {
		var profile = updateCustomerProfileUseCase.updateCustomerProfile(new UpdateCustomerProfileCommand(
				new CustomerId(customerId),
				request.mobileNumber(),
				request.firstName(),
				request.lastName(),
				request.expectedVersion()));
		return ResponseEntity.ok(CustomerResponse.from(profile));
	}

	@GetMapping("/admin/v1/customers")
	@Operation(summary = "Search customers for administration")
	@ApiResponse(responseCode = "200", description = "Customers returned", content = @Content(
			mediaType = MediaType.APPLICATION_JSON_VALUE,
			schema = @Schema(implementation = CustomerPageResponse.class)))
	@ApiResponse(responseCode = "400", description = "Invalid query parameter", content = @Content(
			mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
			schema = @Schema(implementation = ProblemDetail.class),
			examples = @ExampleObject(
					name = "validation-error",
					summary = "Query parameter validation failure",
					value = VALIDATION_PROBLEM_EXAMPLE)))
	ResponseEntity<CustomerPageResponse> listCustomers(
			@RequestParam(required = false) CustomerStatus status,
			@RequestParam(required = false) @Email String email,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestParam(required = false) List<String> sort) {
		var query = new ListCustomersQuery(
				status,
				email,
				page,
				size,
				parseSort(sort));
		return ResponseEntity.ok(CustomerPageResponse.from(listCustomersUseCase.listCustomers(query)));
	}

	private static List<CustomerSortOrder> parseSort(List<String> sort) {
		if (sort == null || sort.isEmpty()) {
			return List.of();
		}

		var tokens = sort.stream()
				.flatMap(value -> List.of(value.split(",", -1)).stream())
				.map(String::trim)
				.toList();
		var orders = new ArrayList<CustomerSortOrder>();
		for (var index = 0; index < tokens.size(); index++) {
			var property = tokens.get(index);
			var direction = CustomerSortOrder.Direction.ASC;
			if (index + 1 < tokens.size() && isSortDirection(tokens.get(index + 1))) {
				direction = parseSortDirection(tokens.get(index + 1));
				index++;
			}
			orders.add(parseSortOrder(property, direction));
		}
		return List.copyOf(orders);
	}

	private static CustomerSortOrder parseSortOrder(String propertyName, CustomerSortOrder.Direction direction) {
		if (propertyName.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort parameter");
		}

		var property = SORT_PROPERTIES.get(propertyName);
		if (property == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sort property: " + propertyName);
		}

		return new CustomerSortOrder(property, direction);
	}

	private static boolean isSortDirection(String direction) {
		var normalizedDirection = direction.toLowerCase(Locale.ROOT);
		return normalizedDirection.equals("asc") || normalizedDirection.equals("desc");
	}

	private static CustomerSortOrder.Direction parseSortDirection(String direction) {
		return switch (direction.toLowerCase(Locale.ROOT)) {
			case "asc" -> CustomerSortOrder.Direction.ASC;
			case "desc" -> CustomerSortOrder.Direction.DESC;
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported sort direction: " + direction);
		};
	}

	private static final Map<String, String> SORT_PROPERTIES = Map.ofEntries(
			Map.entry("customerId", "customerId"),
			Map.entry("email", "email"),
			Map.entry("mobileNumber", "mobileNumber"),
			Map.entry("firstName", "firstName"),
			Map.entry("lastName", "lastName"),
			Map.entry("status", "status"),
			Map.entry("createdAt", "createdAt"),
			Map.entry("updatedAt", "updatedAt"));

	private static final String VALIDATION_PROBLEM_EXAMPLE = """
			{
			  "type": "about:blank",
			  "title": "Invalid request",
			  "status": 400,
			  "detail": "Request validation failed",
			  "errors": [
			    {
			      "field": "email",
			      "message": "must be a well-formed email address"
			    }
			  ]
			}
			""";

	private static final String CUSTOMER_CONFLICT_PROBLEM_EXAMPLE = """
			{
			  "type": "about:blank",
			  "title": "Customer already exists",
			  "status": 409,
			  "detail": "Customer email is already registered",
			  "field": "email"
			}
			""";

	private static final String CUSTOMER_NOT_FOUND_PROBLEM_EXAMPLE = """
			{
			  "type": "about:blank",
			  "title": "Customer not found",
			  "status": 404,
			  "detail": "Customer was not found",
			  "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
			}
			""";

	private static final String PROFILE_CONFLICT_PROBLEM_EXAMPLE = """
			{
			  "type": "about:blank",
			  "title": "Customer profile conflict",
			  "status": 409,
			  "detail": "Customer profile version is stale",
			  "currentVersion": 2,
			  "expectedVersion": 1
			}
			""";
}
