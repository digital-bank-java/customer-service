package com.digitalbank.customerservice.adapter.in.web;

import com.digitalbank.customerservice.application.port.in.PaginatedCustomerProfiles;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Paginated customer search result")
record CustomerPageResponse(
        @Schema(description = "Returned customer page") List<CustomerResponse> items,

        @Schema(description = "Zero-based page number") int pageNumber,

        @Schema(description = "Effective page size") int pageSize,

        @Schema(description = "Total number of matching customers")
        long totalElements,

        @Schema(description = "Total number of pages") int totalPages,

        @Schema(description = "Whether this is the last page")
        boolean last) {

    static CustomerPageResponse from(PaginatedCustomerProfiles page) {
        return new CustomerPageResponse(
                page.items().stream().map(CustomerResponse::from).toList(),
                page.pageNumber(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages(),
                page.last());
    }
}
