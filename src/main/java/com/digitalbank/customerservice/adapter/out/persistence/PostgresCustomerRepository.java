package com.digitalbank.customerservice.adapter.out.persistence;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.digitalbank.customerservice.application.model.CustomerSortOrder;
import com.digitalbank.customerservice.application.port.out.CustomerSearchCriteria;
import com.digitalbank.customerservice.application.port.out.CustomerSearchResult;
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
	public CustomerSearchResult search(CustomerSearchCriteria criteria) {
		var pageRequest = PageRequest.of(
				criteria.pageNumber(),
				criteria.pageSize(),
				sortFor(criteria));
		var page = repository.findAll(specificationFor(criteria), pageRequest);
		var customers = page.getContent().stream()
				.map(CustomerJpaMapper::toDomain)
				.toList();

		return new CustomerSearchResult(
				customers,
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages(),
				page.isLast());
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

	private static Sort sortFor(CustomerSearchCriteria criteria) {
		if (criteria.sort() == null || criteria.sort().isEmpty()) {
			return Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.ASC, "customerId"));
		}

		var orders = criteria.sort().stream()
				.map(PostgresCustomerRepository::toOrder)
				.toList();
		return Sort.by(orders);
	}

	private static Sort.Order toOrder(CustomerSortOrder sortOrder) {
		var direction = switch (sortOrder.direction()) {
			case ASC -> Sort.Direction.ASC;
			case DESC -> Sort.Direction.DESC;
		};
		return new Sort.Order(direction, sortOrder.property());
	}

	private static Specification<CustomerJpaEntity> specificationFor(CustomerSearchCriteria criteria) {
		return (root, query, builder) -> {
			var predicate = builder.conjunction();
			if (criteria.status() != null) {
				predicate = builder.and(predicate, builder.equal(root.get("status"), criteria.status()));
			}
			if (criteria.email() != null) {
				predicate = builder.and(predicate, builder.equal(root.get("email"), criteria.email()));
			}
			return predicate;
		};
	}
}
