package com.github.switcherapi.client.model;

/**
 * Creates the input used after by invoking isItOn().
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class Entry {
	
	private String strategy;
	
	private String input;
	
	private Entry(final String strategy, final String input) {
		this.strategy = strategy;
		this.input = input;
	}
	
	private Entry(final StrategyValidator strategy, final String input) {
		this(strategy.toString(), input);
	}
	
	/**
	 * @param strategy Validator used to evaluate the Switcher
	 * @param input follow the required format documented into each strategy type
	 * @return new Entry
	 * @see StrategyValidator
	 */
	public static Entry build(final StrategyValidator strategy, final String input) {
		return new Entry(strategy, input);
	}
	
	public static Entry build(final String strategy, final String input) {
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
			
			if (!this.input.equals(entry.getInput())) {
				return false;
			}
		}
		return true;
	}

}
