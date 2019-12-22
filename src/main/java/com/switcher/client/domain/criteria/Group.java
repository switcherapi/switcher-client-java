package com.switcher.client.domain.criteria;

import java.util.Arrays;

public class Group extends SwitcherElement {
	
	private Config[] config;

	public Config[] getConfig() {
		
		return config;
	}

	public void setConfig(Config[] config) {
		
		this.config = config;
	}

	@Override
	public String toString() {
		
		return "Group [config=" + Arrays.toString(config) + ", description=" + description + ", activated=" + activated
				+ "]";
	}
	
}
