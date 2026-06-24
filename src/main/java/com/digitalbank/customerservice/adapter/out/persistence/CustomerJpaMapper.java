package com.digitalbank.customerservice.adapter.out.persistence;

import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerId;

final class CustomerJpaMapper {

	private CustomerJpaMapper() {
	}

	static CustomerJpaEntity toEntity(Customer customer) {
		return new CustomerJpaEntity(
				customer.id().value(),
				customer.email(),
				customer.mobileNumber(),
				customer.firstName(),
				customer.lastName(),
				customer.dateOfBirth(),
				customer.status(),
				customer.version(),
				customer.createdAt(),
				customer.updatedAt());
	}

	static Customer toDomain(CustomerJpaEntity entity) {
		return new Customer(
				new CustomerId(entity.customerId()),
				entity.email(),
				entity.mobileNumber(),
				entity.firstName(),
				entity.lastName(),
				entity.dateOfBirth(),
				entity.status(),
				entity.version(),
				entity.createdAt(),
				entity.updatedAt());
	}
}
