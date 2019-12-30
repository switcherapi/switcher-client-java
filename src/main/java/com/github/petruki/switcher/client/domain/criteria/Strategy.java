package com.github.petruki.switcher.client.domain.criteria;

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

	@Override
	public String toString() {
		
		return "Strategy [strategy=" + strategy + ", operation=" + operation + ", values=" + Arrays.toString(values)
				+ ", description=" + description + ", activated=" + activated + "]";
	}
	
}
