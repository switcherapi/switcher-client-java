package com.github.switcherapi.client.model;

/**
 * @author Roger Floriano (petruki)
 * @since 2022-06-12
 */
public enum StrategyValidator {
	
	/**
	 * Execute a value validation using a provided input
	 */
	VALUE("VALUE_VALIDATION"),
	
	/**
	 * Execute a numeric validation using a provided input
	 */
	NUMERIC("NUMERIC_VALIDATION"),
	
	/**
	 * Execute a network validation using a provided input.
	 * <br>- It works only with IPv4 format.
	 */
	NETWORK("NETWORK_VALIDATION"),
	
	/**
	 * Execute a date validation using a provided input.
	 * <br>
	 * <br>- Use the following format:
	 * <br> <b>YYYY-mm-dd hh:mm:ss</b>: 2019-12-10 16:00:00
	 * <br> or
	 * <br> <b>YYYY-mm-dd</b>: 2019-12-10
	 */
	DATE("DATE_VALIDATION"),
	
	/**
	 * Execute a time validation using a provided input.
	 * <br>
	 * <br>- Use the following format:
	 * <br> <b>hh:mm</b>: 16:00
	 */
	TIME("TIME_VALIDATION"),
	
	/**
	 * Execute a regular expression based validation
	 */
	REGEX("REGEX_VALIDATION"),
	
	INVALID("INVALID");
	
	private String validator;
	
	private StrategyValidator(String validator) {
		this.validator = validator;
	}
	
	@Override
	public String toString() {
		return validator;
	}

}
