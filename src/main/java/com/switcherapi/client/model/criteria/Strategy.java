package com.switcherapi.client.model.criteria;

import java.util.Arrays;

import com.switcherapi.client.model.EntryOperation;
import com.switcherapi.client.model.StrategyValidator;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class Strategy extends SwitcherElement {

	private final String strategy;

	private final String operation;

	private final String[] values;

	public Strategy(String strategy, String operation, String description, boolean activated, String[] values) {
		super(description, activated);
		this.strategy = strategy;
		this.operation = operation;
		this.values = values;
	}

	public EntryOperation getEntryOperation() {
		return Arrays.stream(EntryOperation.values())
				.filter(o -> o.toString().equals(this.operation))
				.findFirst()
				.orElse(EntryOperation.INVALID);
	}
	
	public StrategyValidator getStrategyValidator() {
		return Arrays.stream(StrategyValidator.values())
				.filter(o -> o.toString().equals(this.strategy))
				.findFirst()
				.orElse(StrategyValidator.INVALID);
	}

	public String getStrategy() {
		return strategy;
	}

	public String getOperation() {
		return operation;
	}

	public String[] getValues() {
		return values;
	}

	@Override
	public String toString() {
		return String.format("Strategy [strategy = %s, operation = %s, description = %s, activated = %s, values = %s]",
				strategy, operation, description, activated, Arrays.toString(values));
	}

}
