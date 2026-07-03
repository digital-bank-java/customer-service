package com.digitalbank.customerservice.application.port.in;

import com.digitalbank.customerservice.domain.model.CustomerId;

public interface GetCustomerProfileUseCase {

    CustomerProfile getCustomerProfile(CustomerId customerId);
}
