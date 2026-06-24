package com.digitalbank.customerservice.domain.model;

import java.time.Instant;
import java.time.LocalDate;

public record Customer(
		CustomerId id,
		String email,
		String mobileNumber,
		String firstName,
		String lastName,
		LocalDate dateOfBirth,
		CustomerStatus status,
		long version,
		Instant createdAt,
		Instant updatedAt) {

	public static Customer createActive(
			CustomerId id,
			String email,
			String mobileNumber,
			String firstName,
			String lastName,
			LocalDate dateOfBirth,
			Instant now) {
		return new Customer(id, email, mobileNumber, firstName, lastName, dateOfBirth, CustomerStatus.ACTIVE, 0L, now, now);
	}
}