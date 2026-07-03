package com.digitalbank.customerservice.application.port.in;

import com.digitalbank.customerservice.application.model.CustomerSortOrder;
import com.digitalbank.customerservice.domain.model.CustomerStatus;
import java.util.List;

public record ListCustomersQuery(
        CustomerStatus status, String email, int pageNumber, int pageSize, List<CustomerSortOrder> sort) {}
