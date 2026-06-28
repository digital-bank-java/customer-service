package com.digitalbank.customerservice.application.port.in;

import java.util.List;

import com.digitalbank.customerservice.application.model.CustomerSortOrder;
import com.digitalbank.customerservice.domain.model.CustomerStatus;

public record ListCustomersQuery(
		CustomerStatus status,
		String email,
		int pageNumber,
		int pageSize,
		List<CustomerSortOrder> sort) {
}
