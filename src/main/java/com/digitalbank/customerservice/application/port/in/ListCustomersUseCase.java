package com.digitalbank.customerservice.application.port.in;

public interface ListCustomersUseCase {

	PaginatedCustomerProfiles listCustomers(ListCustomersQuery query);
}
