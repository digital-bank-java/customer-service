package com.digitalbank.customerservice.application.port.in;

import java.time.LocalDate;

public record RegisterCustomerCommand(
		String email,
		String mobileNumber,
		String firstName,
		String lastName,
		LocalDate dateOfBirth) {
}
