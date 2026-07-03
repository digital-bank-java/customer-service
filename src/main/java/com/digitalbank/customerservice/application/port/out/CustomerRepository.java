package com.digitalbank.customerservice.application.port.out;

import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerId;
import java.util.Optional;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(CustomerId customerId);

    CustomerSearchResult search(CustomerSearchCriteria criteria);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByMobileNumberForAnotherCustomer(String mobileNumber, CustomerId customerId);
}
