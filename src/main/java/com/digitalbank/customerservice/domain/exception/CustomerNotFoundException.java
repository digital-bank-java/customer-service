package com.digitalbank.customerservice.domain.exception;

import com.digitalbank.customerservice.domain.model.CustomerId;

public final class CustomerNotFoundException extends RuntimeException {

    private final CustomerId customerId;

    public CustomerNotFoundException(CustomerId customerId) {
        super("Customer was not found");
        this.customerId = customerId;
    }

    public CustomerId customerId() {
        return customerId;
    }
}
