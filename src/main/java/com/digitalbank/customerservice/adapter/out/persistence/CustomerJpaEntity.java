package com.digitalbank.customerservice.adapter.out.persistence;

import com.digitalbank.customerservice.domain.model.CustomerStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
class CustomerJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 32)
    private String mobileNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private CustomerStatus status;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CustomerJpaEntity() {}

    CustomerJpaEntity(
            UUID customerId,
            String email,
            String mobileNumber,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            CustomerStatus status,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        this.customerId = customerId;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID customerId() {
        return customerId;
    }

    String email() {
        return email;
    }

    String mobileNumber() {
        return mobileNumber;
    }

    String firstName() {
        return firstName;
    }

    String lastName() {
        return lastName;
    }

    LocalDate dateOfBirth() {
        return dateOfBirth;
    }

    CustomerStatus status() {
        return status;
    }

    long version() {
        return version;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant updatedAt() {
        return updatedAt;
    }
}
