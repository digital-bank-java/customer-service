package com.digitalbank.customerservice.domain.exception;

import com.digitalbank.customerservice.domain.model.CustomerId;

public final class CustomerVersionConflictException extends RuntimeException {

    private final CustomerId customerId;
    private final long currentVersion;
    private final long expectedVersion;

    public CustomerVersionConflictException(CustomerId customerId, long currentVersion, long expectedVersion) {
        super("Customer profile version is stale");
        this.customerId = customerId;
        this.currentVersion = currentVersion;
        this.expectedVersion = expectedVersion;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public long currentVersion() {
        return currentVersion;
    }

    public long expectedVersion() {
        return expectedVersion;
    }
}
