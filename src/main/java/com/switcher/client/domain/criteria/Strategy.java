package com.switcher.client.domain.criteria;

import java.util.Arrays;

public class Strategy extends SwitcherElement {
	
	private String strategy;
	private String operation;
	private String[] values;
	
	public String getStrategy() {
		
		return strategy;
	}
	
	public void setStrategy(String strategy) {
		
		this.strategy = strategy;
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

	public void setValues(String[] values) {
		
		this.values = values;
	}

	@Override
	public String toString() {
		
		return "Strategy [strategy=" + strategy + ", operation=" + operation + ", values=" + Arrays.toString(values)
				+ ", description=" + description + ", activated=" + activated + "]";
	}
	
}
