package com.switcher.client.domain.criteria;

abstract class SwitcherElement {
	
	protected String description;
	protected boolean activated;
	
	public String getDescription() {
		
		return description;
	}

	public void setDescription(String description) {
		
		this.description = description;
	}

	public boolean isActivated() {
		
		return activated;
	}
	
	public void setActivated(boolean activated) {
		
		this.activated = activated;
	}

}
