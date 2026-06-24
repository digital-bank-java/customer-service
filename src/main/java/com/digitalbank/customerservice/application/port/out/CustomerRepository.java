package com.digitalbank.customerservice.application.port.out;

import java.util.Optional;

import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerId;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(CustomerId customerId);
}