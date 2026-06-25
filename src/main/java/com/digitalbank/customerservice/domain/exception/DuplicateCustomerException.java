package com.digitalbank.customerservice.domain.exception;

public final class DuplicateCustomerException extends RuntimeException {

	private final String field;

	private DuplicateCustomerException(String field, String message) {
		super(message);
		this.field = field;
	}

	public static DuplicateCustomerException email() {
		return new DuplicateCustomerException("email", "Customer email is already registered");
	}

	public static DuplicateCustomerException mobileNumber() {
		return new DuplicateCustomerException("mobileNumber", "Customer mobile number is already registered");
	}

	public String field() {
		return field;
	}
}
