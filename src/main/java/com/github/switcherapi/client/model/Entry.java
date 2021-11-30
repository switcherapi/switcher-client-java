package com.github.switcherapi.client.model;

/**
 * Creates the input used after by invoking isItOn().
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class Entry {
	
	/**
	 * Execute a value validation using a provided input
	 */
	public static final String VALUE = "VALUE_VALIDATION";
	
	/**
	 * Execute a numeric validation using a provided input
	 */
	public static final String NUMERIC = "NUMERIC_VALIDATION";
	
	/**
	 * Execute a network validation using a provided input.
	 * <br>- It works only with IPv4 format.
	 */
	public static final String NETWORK = "NETWORK_VALIDATION";
	
	/**
	 * Execute a date validation using a provided input.
	 * <br>
	 * <br>- Use the following format:
	 * <br> <b>YYYY-mm-dd hh:mm:ss</b>: 2019-12-10 16:00:00
	 * <br> or
	 * <br> <b>YYYY-mm-dd</b>: 2019-12-10
	 */
	public static final String DATE = "DATE_VALIDATION";
	
	/**
	 * Execute a time validation using a provided input.
	 * <br>
	 * <br>- Use the following format:
	 * <br> <b>hh:mm</b>: 16:00
	 */
	public static final String TIME = "TIME_VALIDATION";
	
	/**
	 * Execute a regular expression based validation
	 */
	public static final String REGEX = "REGEX_VALIDATION";
	
	public static final String EQUAL = "EQUAL";
	public static final String NOT_EQUAL = "NOT_EQUAL";
	public static final String EXIST = "EXIST";
	public static final String NOT_EXIST = "NOT_EXIST";
	public static final String GREATER = "GREATER";
	public static final String LOWER = "LOWER";
	public static final String BETWEEN = "BETWEEN";
	
	private String strategy;
	private String input;
	
	/**
	 * @param strategy Use one of the constants {@link #VALUE}, {@link #NETWORK}, {@link #DATE}, {@link #TIME}
	 * @param input follow the required format documented into each strategy type
	 */
	public Entry(final String strategy, final String input) {
		this.strategy = strategy;
		this.input = input;
	}
	
	public String getStrategy() {
		return strategy;
	}
	
	public String getInput() {
		return input;
	}

	@Override
	public String toString() {
		return String.format("Entry [strategy = %s, input = %s]", 
				strategy, input);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((strategy == null) ? 0 : strategy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Entry) {
			final Entry entry = (Entry) obj;
			
			if (!this.strategy.equals(entry.getStrategy())) {
				return false;
			}
			
			if (!this.input.equals(entry.getInput())) {
				return false;
			}
		}
		return true;
	}

}
