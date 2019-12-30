package com.github.petruki.switcher.client.domain.criteria;

import java.util.Arrays;

/**
 * @author rogerio
 * @since 2019-12-24
 */
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
