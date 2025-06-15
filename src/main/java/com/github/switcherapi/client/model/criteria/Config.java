package com.github.switcherapi.client.model.criteria;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class Config extends SwitcherElement {

	private final String key;

	private final Strategy[] strategies;

	private final String[] components;

	private final Relay relay;

	public Config(String key, String description, boolean activated, Strategy[] strategies, String[] components,
			Relay relay) {
		super(description, activated);
		this.key = key;
		this.strategies = strategies;
		this.components = components;
		this.relay = relay;
	}

	public boolean hasRelayEnabled() {
		return Objects.nonNull(relay) && relay.isActivated();
	}

	public String getKey() {
		return key;
	}

	public Relay getRelay() {
		return relay;
	}

	public Strategy[] getStrategies() {
		return strategies;
	}

	public String[] getComponents() {
		return components;
	}

	@Override
	public String toString() {
		return String.format("Config [key = %s, description = %s, activated = %s, strategies = %s, components = %s]",
				key, description, activated, Arrays.toString(strategies), Arrays.toString(components));
	}

}
