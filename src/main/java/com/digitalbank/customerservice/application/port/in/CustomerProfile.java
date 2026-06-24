package com.digitalbank.customerservice.application.port.in;

import java.time.Instant;
import java.time.LocalDate;

import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerStatus;

public record CustomerProfile(
		String customerId,
		String email,
		String mobileNumber,
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		CustomerStatus status,
		long version,
		Instant createdAt,
		Instant updatedAt) {

	public static CustomerProfile from(Customer customer) {
		return new CustomerProfile(
				customer.id().value().toString(),
				customer.email(),
				customer.mobileNumber(),
				customer.firstName(),
				customer.lastName(),
				customer.dateOfBirth(),
				customer.status(),
				customer.version(),
				customer.createdAt(),
				customer.updatedAt());
	}
}
