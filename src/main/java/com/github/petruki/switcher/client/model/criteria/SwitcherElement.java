package com.github.petruki.switcher.client.model.criteria;

/**
 * @author rogerio
 * @since 2019-12-24
 */
abstract class SwitcherElement {
	
	protected String description;
	
	protected boolean activated;
	
	public String getDescription() {
		
		return description;
	}
	
	public boolean isActivated() {
		
		return activated;
	}

	public void setDescription(String description) {
		
		this.description = description;
	}

	public void setActivated(boolean activated) {
		
		this.activated = activated;
	}

}
