package com.digitalbank.customerservice.application.port.in;

public interface RegisterCustomerUseCase {

    CustomerProfile registerCustomer(RegisterCustomerCommand command);
}
