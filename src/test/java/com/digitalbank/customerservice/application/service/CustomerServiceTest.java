package com.digitalbank.customerservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.digitalbank.customerservice.application.port.in.ListCustomersQuery;
import com.digitalbank.customerservice.application.port.in.RegisterCustomerCommand;
import com.digitalbank.customerservice.application.port.in.UpdateCustomerProfileCommand;
import com.digitalbank.customerservice.application.port.out.CustomerRepository;
import com.digitalbank.customerservice.application.port.out.CustomerSearchCriteria;
import com.digitalbank.customerservice.application.port.out.CustomerSearchResult;
import com.digitalbank.customerservice.domain.exception.CustomerNotFoundException;
import com.digitalbank.customerservice.domain.exception.CustomerVersionConflictException;
import com.digitalbank.customerservice.domain.exception.DuplicateCustomerException;
import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerId;
import com.digitalbank.customerservice.domain.model.CustomerStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CustomerServiceTest {

    private final InMemoryCustomerRepository customerRepository = new InMemoryCustomerRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:15:30Z"), ZoneOffset.UTC);
    private final CustomerService service = new CustomerService(customerRepository, clock);

    @Test
    void registersActiveCustomer() {
        var profile = service.registerCustomer(new RegisterCustomerCommand(
                " Customer@Example.COM ", " +971501234567 ", " Rami ", " Customer ", LocalDate.parse("1990-01-01")));

        assertThat(profile.customerId()).isNotBlank();
        assertThat(profile.email()).isEqualTo("customer@example.com");
        assertThat(profile.mobileNumber()).isEqualTo("+971501234567");
        assertThat(profile.firstName()).isEqualTo("Rami");
        assertThat(profile.lastName()).isEqualTo("Customer");
        assertThat(profile.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(profile.createdAt()).isEqualTo(clock.instant());
        assertThat(profile.updatedAt()).isEqualTo(clock.instant());
    }

    @Test
    void rejectsDuplicateEmail() {
        service.registerCustomer(validCommand("customer@example.com", "+971501234567"));

        assertThatThrownBy(() -> service.registerCustomer(validCommand("CUSTOMER@example.com", "+971509999999")))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessage("Customer email is already registered");
    }

    @Test
    void rejectsDuplicateMobileNumber() {
        service.registerCustomer(validCommand("customer@example.com", "+971501234567"));

        assertThatThrownBy(() -> service.registerCustomer(validCommand("other@example.com", "+971501234567")))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessage("Customer mobile number is already registered");
    }

    @Test
    void rejectsMissingCustomerProfile() {
        assertThatThrownBy(() -> service.getCustomerProfile(CustomerId.newId()))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer was not found");
    }

    @Test
    void listsCustomersForAdministration() {
        service.registerCustomer(validCommand("customer@example.com", "+971501234567"));
        service.registerCustomer(validCommand("other@example.com", "+971509999999"));

        var page = service.listCustomers(
                new ListCustomersQuery(CustomerStatus.ACTIVE, " CUSTOMER@example.com ", 0, 20, List.of()));

        assertThat(page.items()).singleElement().satisfies(profile -> {
            assertThat(profile.email()).isEqualTo("customer@example.com");
            assertThat(profile.status()).isEqualTo(CustomerStatus.ACTIVE);
        });
        assertThat(page.pageNumber()).isZero();
        assertThat(page.pageSize()).isEqualTo(20);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.last()).isTrue();
    }

    @Test
    void updatesCustomerProfileWhenVersionMatches() {
        var registered = service.registerCustomer(validCommand("customer@example.com", "+971501234567"));

        var updated = service.updateCustomerProfile(new UpdateCustomerProfileCommand(
                new CustomerId(java.util.UUID.fromString(registered.customerId())),
                "+971509999999",
                "Updated",
                "Customer",
                registered.version()));

        assertThat(updated.mobileNumber()).isEqualTo("+971509999999");
        assertThat(updated.firstName()).isEqualTo("Updated");
        assertThat(updated.updatedAt()).isEqualTo(clock.instant());
    }

    @Test
    void rejectsStaleProfileUpdate() {
        var registered = service.registerCustomer(validCommand("customer@example.com", "+971501234567"));

        assertThatThrownBy(() -> service.updateCustomerProfile(new UpdateCustomerProfileCommand(
                        new CustomerId(java.util.UUID.fromString(registered.customerId())),
                        "+971509999999",
                        "Updated",
                        "Customer",
                        99L)))
                .isInstanceOf(CustomerVersionConflictException.class)
                .hasMessage("Customer profile version is stale");
    }

    private static RegisterCustomerCommand validCommand(String email, String mobileNumber) {
        return new RegisterCustomerCommand(email, mobileNumber, "Rami", "Customer", LocalDate.parse("1990-01-01"));
    }

    private static final class InMemoryCustomerRepository implements CustomerRepository {

        private final Map<CustomerId, Customer> customers = new HashMap<>();

        @Override
        public Customer save(Customer customer) {
            customers.put(customer.id(), customer);
            return customer;
        }

        @Override
        public Optional<Customer> findById(CustomerId customerId) {
            return Optional.ofNullable(customers.get(customerId));
        }

        @Override
        public CustomerSearchResult search(CustomerSearchCriteria criteria) {
            var matches = customers.values().stream()
                    .filter(customer ->
                            criteria.status() == null || customer.status().equals(criteria.status()))
                    .filter(customer ->
                            criteria.email() == null || customer.email().equals(criteria.email()))
                    .toList();
            return new CustomerSearchResult(
                    matches,
                    criteria.pageNumber(),
                    criteria.pageSize(),
                    matches.size(),
                    matches.isEmpty() ? 0 : 1,
                    true);
        }

        @Override
        public boolean existsByEmail(String email) {
            return customers.values().stream()
                    .anyMatch(customer -> customer.email().equals(email));
        }

        @Override
        public boolean existsByMobileNumber(String mobileNumber) {
            return customers.values().stream()
                    .anyMatch(customer -> customer.mobileNumber().equals(mobileNumber));
        }

        @Override
        public boolean existsByMobileNumberForAnotherCustomer(String mobileNumber, CustomerId customerId) {
            return customers.values().stream()
                    .anyMatch(customer -> customer.mobileNumber().equals(mobileNumber)
                            && !customer.id().equals(customerId));
        }
    }
}
