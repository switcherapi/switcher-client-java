package com.switcher.client.domain.criteria;

import java.util.Arrays;

public class Config extends SwitcherElement {
	
	private String key;
	private Strategy[] strategies;
	
	public Config() {}
	
	public String getKey() {
		
		return key;
	}
	
	public void setKey(String key) {
		
		this.key = key;
	}
	
	public Strategy[] getStrategies() {
		
		return strategies;
	}
	
	public void setStrategies(Strategy[] strategies) {
		
		this.strategies = strategies;
	}

	@Override
	public String toString() {
		
		return "Config [key=" + key + ", strategies=" + Arrays.toString(strategies) + ", description=" + description
				+ ", activated=" + activated + "]";
	}
	
}
