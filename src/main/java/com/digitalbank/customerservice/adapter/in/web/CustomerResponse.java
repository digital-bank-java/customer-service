package com.digitalbank.customerservice.adapter.in.web;

import java.time.Instant;
import java.time.LocalDate;

import com.digitalbank.customerservice.application.port.in.CustomerProfile;
import com.digitalbank.customerservice.domain.model.CustomerStatus;

record CustomerResponse(
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

	static CustomerResponse from(CustomerProfile profile) {
		return new CustomerResponse(
				profile.customerId(),
				profile.email(),
				profile.mobileNumber(),
				profile.firstName(),
				profile.lastName(),
				profile.dateOfBirth(),
				profile.status(),
				profile.version(),
				profile.createdAt(),
				profile.updatedAt());
	}
}
