package com.digitalbank.customerservice.application.port.in;

import java.util.List;

public record PaginatedCustomerProfiles(
        List<CustomerProfile> items, int pageNumber, int pageSize, long totalElements, int totalPages, boolean last) {}
