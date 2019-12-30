package com.github.petruki.switcher.client.domain.criteria;

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

}
