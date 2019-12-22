package com.switcher.client.domain;

public class Entry {
	
	public static final String VALUE = "VALUE_VALIDATION";
	public static final String NETWORK = "NETWORK_VALIDATION";
	public static final String DATE = "DATE_VALIDATION";
	public static final String TIME = "TIME_VALIDATION";
	
	public static final String EQUAL = "EQUAL";
	public static final String NOT_EQUAL = "NOT_EQUAL";
	public static final String EXIST = "EXIST";
	public static final String NOT_EXIST = "NOT_EXIST";
	public static final String GREATER = "GREATER";
	public static final String LOWER = "LOWER";
	public static final String BETWEEN = "BETWEEN";
	
	private String strategy;
	private String input;
	
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
		
		return "Entry [strategy=" + strategy + ", input=" + input + "]";
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
			
			if (this.strategy.equals(entry.getStrategy())) {
				return true;
			}
		}
		return false;
	}

}
