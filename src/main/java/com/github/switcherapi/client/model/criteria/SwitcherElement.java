package com.github.switcherapi.client.model.criteria;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
abstract class SwitcherElement {

	protected final String description;

	protected final boolean activated;

	protected SwitcherElement(String description, boolean activated) {
		this.description = description;
		this.activated = activated;
	}

	public String getDescription() {
		return description;
	}

	public boolean isActivated() {
		return activated;
	}

}
