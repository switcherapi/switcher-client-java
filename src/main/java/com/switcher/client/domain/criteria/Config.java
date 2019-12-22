package com.switcher.client.domain.criteria;

import java.util.Arrays;

public class Config extends SwitcherElement {
	
	private String key;
	private Strategy[] strategies;
	
	public String getKey() {
		
		return key;
	}
	
	public Strategy[] getStrategies() {
		
		return strategies;
	}

	@Override
	public String toString() {
		
		return "Config [key=" + key + ", strategies=" + Arrays.toString(strategies) + ", description=" + description
				+ ", activated=" + activated + "]";
	}
	
}
