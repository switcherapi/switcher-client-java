package com.github.petruki.switcher.client.model.criteria;

import java.util.Arrays;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class Config extends SwitcherElement {
	
	private String key;
	
	private Strategy[] strategies;
	
	private String[] components;
	
	public String getKey() {
		
		return key;
	}
	
	public Strategy[] getStrategies() {
		
		return strategies;
	}

	public void setKey(String key) {
		
		this.key = key;
	}

	public void setStrategies(Strategy[] strategies) {
		
		this.strategies = strategies;
	}

	public String[] getComponents() {
		
		return components;
	}

	public void setComponents(String[] components) {
		
		this.components = components;
	}

	@Override
	public String toString() {
		
		return String.format("Config [key = %s, description = %s, activated = %s, strategies = %s, components = %s]", 
				key, description, activated, Arrays.toString(strategies), Arrays.toString(components));
	}
	
}
