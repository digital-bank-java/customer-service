package com.digitalbank.customerservice.application.port.in;

import com.digitalbank.customerservice.domain.model.CustomerId;

public record UpdateCustomerProfileCommand(
        CustomerId customerId, String mobileNumber, String firstName, String lastName, long expectedVersion) {}
