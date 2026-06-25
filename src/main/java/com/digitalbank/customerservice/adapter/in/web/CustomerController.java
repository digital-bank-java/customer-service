package com.digitalbank.customerservice.adapter.in.web;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.digitalbank.customerservice.application.port.in.GetCustomerProfileUseCase;
import com.digitalbank.customerservice.application.port.in.RegisterCustomerCommand;
import com.digitalbank.customerservice.application.port.in.RegisterCustomerUseCase;
import com.digitalbank.customerservice.application.port.in.UpdateCustomerProfileCommand;
import com.digitalbank.customerservice.application.port.in.UpdateCustomerProfileUseCase;
import com.digitalbank.customerservice.domain.model.CustomerId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers")
class CustomerController {

	private final RegisterCustomerUseCase registerCustomerUseCase;
	private final GetCustomerProfileUseCase getCustomerProfileUseCase;
	private final UpdateCustomerProfileUseCase updateCustomerProfileUseCase;

	CustomerController(
			RegisterCustomerUseCase registerCustomerUseCase,
			GetCustomerProfileUseCase getCustomerProfileUseCase,
			UpdateCustomerProfileUseCase updateCustomerProfileUseCase) {
		this.registerCustomerUseCase = registerCustomerUseCase;
		this.getCustomerProfileUseCase = getCustomerProfileUseCase;
		this.updateCustomerProfileUseCase = updateCustomerProfileUseCase;
	}

	@PostMapping
	@Operation(summary = "Register a customer")
	@ApiResponse(responseCode = "201", description = "Customer registered")
	@ApiResponse(responseCode = "400", description = "Invalid request")
	@ApiResponse(responseCode = "409", description = "Customer already exists")
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

	@GetMapping("/{customerId}")
	@Operation(summary = "Get a customer profile")
	@ApiResponse(responseCode = "200", description = "Customer profile returned")
	@ApiResponse(responseCode = "404", description = "Customer not found")
	ResponseEntity<CustomerResponse> getCustomerProfile(@PathVariable UUID customerId) {
		var profile = getCustomerProfileUseCase.getCustomerProfile(new CustomerId(customerId));
		return ResponseEntity.ok(CustomerResponse.from(profile));
	}

	@PatchMapping("/{customerId}/profile")
	@Operation(summary = "Update customer profile")
	@ApiResponse(responseCode = "200", description = "Customer profile updated")
	@ApiResponse(responseCode = "400", description = "Invalid request")
	@ApiResponse(responseCode = "404", description = "Customer not found")
	@ApiResponse(responseCode = "409", description = "Duplicate or stale update")
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
}
