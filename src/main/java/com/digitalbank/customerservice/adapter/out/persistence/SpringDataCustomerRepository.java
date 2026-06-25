package com.digitalbank.customerservice.adapter.out.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataCustomerRepository extends JpaRepository<CustomerJpaEntity, UUID> {

	boolean existsByEmail(String email);

	boolean existsByMobileNumber(String mobileNumber);

	boolean existsByMobileNumberAndCustomerIdNot(String mobileNumber, UUID customerId);
}
