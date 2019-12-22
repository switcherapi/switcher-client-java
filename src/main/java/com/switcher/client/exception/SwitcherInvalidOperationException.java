package com.switcher.client.exception;

public class SwitcherInvalidOperationException extends Exception {
	
	private static final long serialVersionUID = 4685056886357966951L;

	public SwitcherInvalidOperationException(final String operation, final String strategyName) {
		
		super(String.format("Something went wrong: Invalid operation %s for %s", operation, strategyName));
	}

}
