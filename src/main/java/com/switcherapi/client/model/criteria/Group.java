package com.switcherapi.client.model.criteria;

import java.util.Arrays;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class Group extends SwitcherElement {

	private final String name;

	private final Config[] config;

	public Group(String name, String description, boolean activated, Config[] config) {
		super(description, activated);
		this.name = name;
		this.config = config;
	}

	public Config[] getConfig() {
		return config;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("Group [name = %s, description = %s, activated = %s, config = %s]", name, description,
				activated, Arrays.toString(config));
	}

}
