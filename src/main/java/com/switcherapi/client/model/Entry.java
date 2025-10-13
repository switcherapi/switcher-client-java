package com.switcherapi.client.model;

/**
 * Creates the input used after by invoking isItOn().
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class Entry {
	
	private final String strategy;
	
	private final String input;

	public Entry(String strategy, String input) {
		this.strategy = strategy;
		this.input = input;
	}

	public Entry(StrategyValidator strategy, String input) {
		this(strategy.toString(), input);
	}
	
	/**
	 * Creates a new Entry with the given strategy and input.
	 *
	 * @param strategy Validator used to evaluate the Switcher
	 * @param input follow the required format documented into each strategy type
	 * @return new Entry
	 * @see StrategyValidator
	 */
	public static Entry of(StrategyValidator strategy, String input) {
		return new Entry(strategy, input);
	}

	public static Entry of(String strategy, String input) {
		return new Entry(strategy, input);
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

			return this.input.equals(entry.getInput());
		}
		return false;
	}

}
