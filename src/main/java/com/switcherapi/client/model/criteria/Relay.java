package com.switcherapi.client.model.criteria;

/**
 * @author Roger Floriano (petruki)
 * @since 2025-06-13
 */
public class Relay {

	private final String type;

	private final boolean activated;

	public Relay(String type, boolean activated) {
		this.type = type;
		this.activated = activated;
	}

	public Relay() {
		this(null, false);
	}

	public boolean isActivated() {
		return activated;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format("Relay [type = %s, activated = %s]", type, activated);
	}

}
