package com.digitalbank.customerservice.adapter.in.web;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.digitalbank.customerservice.domain.exception.CustomerNotFoundException;
import com.digitalbank.customerservice.domain.exception.CustomerVersionConflictException;
import com.digitalbank.customerservice.domain.exception.DuplicateCustomerException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(DuplicateCustomerException.class)
	ResponseEntity<ProblemDetail> handleDuplicateCustomer(DuplicateCustomerException exception) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("Customer already exists");
		problem.setType(URI.create("https://digital-bank-java.local/problems/customer-already-exists"));
		problem.setProperty("field", exception.field());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	ResponseEntity<ProblemDetail> handleCustomerNotFound(CustomerNotFoundException exception) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setTitle("Customer not found");
		problem.setType(URI.create("https://digital-bank-java.local/problems/customer-not-found"));
		problem.setProperty("customerId", exception.customerId().value());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(CustomerVersionConflictException.class)
	ResponseEntity<ProblemDetail> handleVersionConflict(CustomerVersionConflictException exception) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
		problem.setTitle("Customer profile conflict");
		problem.setType(URI.create("https://digital-bank-java.local/problems/customer-version-conflict"));
		problem.setProperty("customerId", exception.customerId().value());
		problem.setProperty("currentVersion", exception.currentVersion());
		problem.setProperty("expectedVersion", exception.expectedVersion());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleValidationFailure(MethodArgumentNotValidException exception) {
		var errors = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> Map.of(
						"field", error.getField(),
						"message", String.valueOf(error.getDefaultMessage())))
				.toList();

		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
		problem.setTitle("Invalid request");
		problem.setType(URI.create("https://digital-bank-java.local/problems/validation-error"));
		problem.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ProblemDetail> handleQueryParameterValidationFailure(ConstraintViolationException exception) {
		var errors = exception.getConstraintViolations().stream()
				.map(violation -> Map.of(
						"field", violation.getPropertyPath().toString(),
						"message", violation.getMessage()))
				.toList();

		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
		problem.setTitle("Invalid request");
		problem.setType(URI.create("https://digital-bank-java.local/problems/validation-error"));
		problem.setProperty("errors", errors);
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(ResponseStatusException.class)
	ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException exception) {
		var problem = ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason());
		problem.setTitle("Invalid request");
		problem.setType(URI.create("https://digital-bank-java.local/problems/validation-error"));
		return ResponseEntity.status(exception.getStatusCode()).body(problem);
	}
}
