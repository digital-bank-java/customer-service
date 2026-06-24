package com.digitalbank.customerservice.adapter.out.persistence;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.digitalbank.customerservice.application.port.out.CustomerRepository;
import com.digitalbank.customerservice.domain.exception.DuplicateCustomerException;
import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerId;

@Repository
class PostgresCustomerRepository implements CustomerRepository {

	private final SpringDataCustomerRepository repository;

	PostgresCustomerRepository(SpringDataCustomerRepository repository) {
		this.repository = repository;
	}

	@Override
	public Customer save(Customer customer) {
		try {
			return CustomerJpaMapper.toDomain(repository.saveAndFlush(CustomerJpaMapper.toEntity(customer)));
		}
		catch (DataIntegrityViolationException exception) {
			throw duplicateCustomer(customer, exception);
		}
	}

	@Override
	public Optional<Customer> findById(CustomerId customerId) {
		return repository.findById(customerId.value()).map(CustomerJpaMapper::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		return repository.existsByEmail(email);
	}

	@Override
	public boolean existsByMobileNumber(String mobileNumber) {
		return repository.existsByMobileNumber(mobileNumber);
	}

	@Override
	public boolean existsByMobileNumberForAnotherCustomer(String mobileNumber, CustomerId customerId) {
		return repository.existsByMobileNumberAndCustomerIdNot(mobileNumber, customerId.value());
	}

	private static DuplicateCustomerException duplicateCustomer(Customer customer, DataIntegrityViolationException exception) {
		var message = String.valueOf(exception.getMostSpecificCause().getMessage());
		if (message.contains("uk_customers_mobile_number")) {
			return DuplicateCustomerException.mobileNumber();
		}
		return DuplicateCustomerException.email();
	}
}
