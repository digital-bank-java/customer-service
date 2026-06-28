package com.digitalbank.customerservice.application.service;

import java.time.Clock;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.digitalbank.customerservice.application.port.in.CustomerProfile;
import com.digitalbank.customerservice.application.port.in.GetCustomerProfileUseCase;
import com.digitalbank.customerservice.application.port.in.ListCustomersQuery;
import com.digitalbank.customerservice.application.port.in.ListCustomersUseCase;
import com.digitalbank.customerservice.application.port.in.PaginatedCustomerProfiles;
import com.digitalbank.customerservice.application.port.in.RegisterCustomerCommand;
import com.digitalbank.customerservice.application.port.in.RegisterCustomerUseCase;
import com.digitalbank.customerservice.application.port.in.UpdateCustomerProfileCommand;
import com.digitalbank.customerservice.application.port.in.UpdateCustomerProfileUseCase;
import com.digitalbank.customerservice.application.port.out.CustomerSearchCriteria;
import com.digitalbank.customerservice.application.port.out.CustomerRepository;
import com.digitalbank.customerservice.domain.exception.CustomerNotFoundException;
import com.digitalbank.customerservice.domain.exception.CustomerVersionConflictException;
import com.digitalbank.customerservice.domain.exception.DuplicateCustomerException;
import com.digitalbank.customerservice.domain.model.Customer;
import com.digitalbank.customerservice.domain.model.CustomerId;

@Service
class CustomerService implements RegisterCustomerUseCase, GetCustomerProfileUseCase, UpdateCustomerProfileUseCase,
		ListCustomersUseCase {

	private final CustomerRepository customerRepository;
	private final Clock clock;

	CustomerService(CustomerRepository customerRepository, Clock clock) {
		this.customerRepository = customerRepository;
		this.clock = clock;
	}

	@Override
	@Transactional
	public CustomerProfile registerCustomer(RegisterCustomerCommand command) {
		var email = normalizeEmail(command.email());
		var mobileNumber = normalizeRequired(command.mobileNumber());

		if (customerRepository.existsByEmail(email)) {
			throw DuplicateCustomerException.email();
		}
		if (customerRepository.existsByMobileNumber(mobileNumber)) {
			throw DuplicateCustomerException.mobileNumber();
		}

		var now = clock.instant();
		var customer = Customer.createActive(
				CustomerId.newId(),
				email,
				mobileNumber,
				normalizeRequired(command.firstName()),
				normalizeRequired(command.lastName()),
				command.dateOfBirth(),
				now);

		return CustomerProfile.fromCustomer(customerRepository.save(customer));
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerProfile getCustomerProfile(CustomerId customerId) {
		return customerRepository.findById(customerId)
				.map(CustomerProfile::fromCustomer)
				.orElseThrow(() -> new CustomerNotFoundException(customerId));
	}

	@Override
	@Transactional(readOnly = true)
	public PaginatedCustomerProfiles listCustomers(ListCustomersQuery query) {
		var result = customerRepository.search(new CustomerSearchCriteria(
				query.status(),
				normalizeOptionalEmail(query.email()),
				query.pageNumber(),
				query.pageSize(),
				query.sort()));
		var items = result.customers().stream()
				.map(CustomerProfile::fromCustomer)
				.toList();

		return new PaginatedCustomerProfiles(
				items,
				result.pageNumber(),
				result.pageSize(),
				result.totalElements(),
				result.totalPages(),
				result.last());
	}

	@Override
	@Transactional
	public CustomerProfile updateCustomerProfile(UpdateCustomerProfileCommand command) {
		var customer = customerRepository.findById(command.customerId())
				.orElseThrow(() -> new CustomerNotFoundException(command.customerId()));
		if (customer.version() != command.expectedVersion()) {
			throw new CustomerVersionConflictException(command.customerId(), customer.version(), command.expectedVersion());
		}

		var mobileNumber = normalizeRequired(command.mobileNumber());
		if (customerRepository.existsByMobileNumberForAnotherCustomer(mobileNumber, command.customerId())) {
			throw DuplicateCustomerException.mobileNumber();
		}

		var updatedCustomer = customer.updateProfile(
				mobileNumber,
				normalizeRequired(command.firstName()),
				normalizeRequired(command.lastName()),
				clock.instant());
		return CustomerProfile.fromCustomer(customerRepository.save(updatedCustomer));
	}

	private static String normalizeEmail(String value) {
		return normalizeRequired(value).toLowerCase(Locale.ROOT);
	}

	private static String normalizeOptionalEmail(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return normalizeEmail(value);
	}

	private static String normalizeRequired(String value) {
		return value == null ? null : value.trim();
	}
}
