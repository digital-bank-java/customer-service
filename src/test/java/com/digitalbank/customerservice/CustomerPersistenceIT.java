package com.digitalbank.customerservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.digitalbank.customerservice.application.port.out.CustomerRepository;
import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerId;
import com.digitalbank.customerservice.domain.model.CustomerStatus;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class CustomerPersistenceIT {

    @Container
    private static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.config.enabled", () -> "false");
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.open-in-view", () -> "false");
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void savesAndLoadsCustomer() {
        var now = Instant.parse("2026-01-01T10:15:30Z");
        var customerId = CustomerId.newId();
        var customer = Customer.createActive(
                customerId,
                "customer@example.com",
                "+971501234567",
                "Rami",
                "Customer",
                LocalDate.parse("1990-01-01"),
                now);

        customerRepository.save(customer);

        var savedCustomer = customerRepository.findById(customerId);

        assertThat(savedCustomer).hasValueSatisfying(saved -> {
            assertThat(saved.id()).isEqualTo(customerId);
            assertThat(saved.email()).isEqualTo("customer@example.com");
            assertThat(saved.mobileNumber()).isEqualTo("+971501234567");
            assertThat(saved.status()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(saved.createdAt()).isEqualTo(now);
            assertThat(saved.updatedAt()).isEqualTo(now);
        });
    }
}
