package com.github.switcherapi.client.model.criteria;

import java.util.Arrays;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class Group extends SwitcherElement {
	
	private String name;
	
	private Config[] config;

	public Config[] getConfig() {
		
		return config;
	}

	public void setConfig(Config[] config) {
		
		this.config = config;
	}

	public String getName() {
		
		return name;
	}

	public void setName(String name) {
		
		this.name = name;
	}

	@Override
	public String toString() {
		
		return String.format("Group [name = %s, description = %s, activated = %s, config = %s]", 
				name, description, activated, Arrays.toString(config));
	}
	
}
