package com.github.petruki.switcher.client.model.criteria;

import java.util.Arrays;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class Strategy extends SwitcherElement {
	
	private String strategy;
	
	private String operation;
	
	private String[] values;
	
	public String getStrategy() {
		
		return strategy;
	}
	
	public String getOperation() {
		
		return operation;
	}
	
	public void setOperation(String operation) {
		
		this.operation = operation;
	}

	public String[] getValues() {
		
		return values;
	}

	public void setStrategy(String strategy) {
		
		this.strategy = strategy;
	}

	public void setValues(String[] values) {
		
		this.values = values;
	}

	@Override
	public String toString() {
		
		return String.format("Strategy [strategy = %s, operation = %s, description = %s, activated = %s, values = %s]", 
				strategy, operation, description, activated, Arrays.toString(values));
	}
	
}
