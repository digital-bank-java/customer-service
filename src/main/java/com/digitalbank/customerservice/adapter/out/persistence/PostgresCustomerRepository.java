package com.digitalbank.customerservice.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.digitalbank.customerservice.application.port.out.CustomerRepository;
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
		return CustomerJpaMapper.toDomain(repository.save(CustomerJpaMapper.toEntity(customer)));
	}

	@Override
	public Optional<Customer> findById(CustomerId customerId) {
		return repository.findById(customerId.value()).map(CustomerJpaMapper::toDomain);
	}
}
