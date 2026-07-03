package com.digitalbank.customerservice.application.port.out;

import com.digitalbank.customerservice.domain.model.Customer;
import java.util.List;

public record CustomerSearchResult(
        List<Customer> customers, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last) {}
