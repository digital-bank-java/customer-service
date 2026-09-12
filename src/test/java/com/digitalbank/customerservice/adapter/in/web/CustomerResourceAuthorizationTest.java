package com.digitalbank.customerservice.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

class CustomerResourceAuthorizationTest {

    private final CustomerResourceAuthorization authorization = new CustomerResourceAuthorization();
    private final UUID customerId = UUID.randomUUID();

    @Test
    void allowsAdminIdentityToAccessAnyCustomer() {
        var authentication = authentication("transfer-orchestrator", "SCOPE_admin.internal");

        authorization.requireCustomerAccess(customerId, authentication);
    }

    @Test
    void allowsSelfScopeOnlyWhenSubjectMatchesCustomerId() {
        var authentication = authentication(customerId.toString(), "SCOPE_customer.self");

        authorization.requireCustomerAccess(customerId, authentication);
    }

    @Test
    void rejectsAuthenticatedIdentityWithoutOwnershipScope() {
        var authentication = authentication("someone-else", "SCOPE_profile.read");

        assertThatThrownBy(() -> authorization.requireCustomerAccess(customerId, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception ->
                        ((ResponseStatusException) exception).getStatusCode().value())
                .isEqualTo(403);
    }

    @Test
    void rejectsSelfScopeForAnotherCustomer() {
        var authentication = authentication(UUID.randomUUID().toString(), "SCOPE_customer.self");

        assertThatThrownBy(() -> authorization.requireCustomerAccess(customerId, authentication))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception ->
                        ((ResponseStatusException) exception).getStatusCode().value())
                .isEqualTo(403);
    }

    private static UsernamePasswordAuthenticationToken authentication(String subject, String scope) {
        return new UsernamePasswordAuthenticationToken(subject, "n/a", List.of(new SimpleGrantedAuthority(scope)));
    }
}
