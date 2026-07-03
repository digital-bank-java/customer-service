package com.digitalbank.customerservice.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record UpdateCustomerProfileRequest(
        @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "must be an E.164 phone number")
        String mobileNumber,

        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull @Min(0) Long expectedVersion) {}
