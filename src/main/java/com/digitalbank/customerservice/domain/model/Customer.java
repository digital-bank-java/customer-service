package com.digitalbank.customerservice.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

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

	public Customer {
		Objects.requireNonNull(id, "Customer id is required");
		email = requireText(email, "Customer email is required");
		mobileNumber = requireText(mobileNumber, "Customer mobile number is required");
		firstName = requireText(firstName, "Customer first name is required");
		lastName = requireText(lastName, "Customer last name is required");
		Objects.requireNonNull(dateOfBirth, "Customer date of birth is required");
		Objects.requireNonNull(status, "Customer status is required");
		Objects.requireNonNull(createdAt, "Customer creation timestamp is required");
		Objects.requireNonNull(updatedAt, "Customer update timestamp is required");
		if (version < 0) {
			throw new IllegalArgumentException("Customer version must not be negative");
		}
		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("Customer update timestamp must not be before creation timestamp");
		}
	}

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

	public Customer updateProfile(String mobileNumber, String firstName, String lastName, Instant now) {
		return new Customer(
				id,
				email,
				mobileNumber,
				firstName,
				lastName,
				dateOfBirth,
				status,
				version,
				createdAt,
				now);
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(message);
		}
		return value;
	}
}
