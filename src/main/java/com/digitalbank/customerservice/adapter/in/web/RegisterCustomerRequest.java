package com.digitalbank.customerservice.adapter.in.web;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record RegisterCustomerRequest(
		@NotBlank @Email @Size(max = 320) String email,
		@NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "must be an E.164 phone number") String mobileNumber,
		@NotBlank @Size(max = 100) String firstName,
		@NotBlank @Size(max = 100) String lastName,
		@NotNull @Past LocalDate dateOfBirth) {
}
