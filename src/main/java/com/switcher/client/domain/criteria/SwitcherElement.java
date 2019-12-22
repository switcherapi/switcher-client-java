package com.switcher.client.domain.criteria;

abstract class SwitcherElement {
	
	protected String description;
	protected boolean activated;
	
	public String getDescription() {
		
		return description;
	}
	
	public boolean isActivated() {
		
		return activated;
	}

}
