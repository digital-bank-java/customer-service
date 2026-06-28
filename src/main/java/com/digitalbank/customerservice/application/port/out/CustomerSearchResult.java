package com.digitalbank.customerservice.application.port.out;

import java.util.List;

import com.digitalbank.customerservice.domain.model.Customer;

public record CustomerSearchResult(
		List<Customer> customers,
		int pageNumber,
		int pageSize,
		long totalElements,
		int totalPages,
		boolean last) {
}
