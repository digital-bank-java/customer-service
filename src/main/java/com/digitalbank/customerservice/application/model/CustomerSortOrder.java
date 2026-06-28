package com.digitalbank.customerservice.application.model;

public record CustomerSortOrder(
		String property,
		Direction direction) {

	public enum Direction {
		ASC,
		DESC
	}
}
