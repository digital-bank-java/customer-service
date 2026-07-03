package com.digitalbank.customerservice.application.port.in;

public interface UpdateCustomerProfileUseCase {

    CustomerProfile updateCustomerProfile(UpdateCustomerProfileCommand command);
}
