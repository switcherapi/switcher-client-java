package com.github.petruki.switcher.client.domain.criteria;

import java.util.Arrays;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class Group extends SwitcherElement {
	
	private Config[] config;

	public Config[] getConfig() {
		
		return config;
	}

	@Override
	public String toString() {
		
		return "Group [config=" + Arrays.toString(config) + ", description=" + description + ", activated=" + activated
				+ "]";
	}
	
}
