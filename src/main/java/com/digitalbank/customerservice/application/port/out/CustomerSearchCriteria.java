package com.digitalbank.customerservice.application.port.out;

import com.digitalbank.customerservice.application.model.CustomerSortOrder;
import com.digitalbank.customerservice.domain.model.CustomerStatus;
import java.util.List;

public record CustomerSearchCriteria(
        CustomerStatus status, String email, int pageNumber, int pageSize, List<CustomerSortOrder> sort) {}
