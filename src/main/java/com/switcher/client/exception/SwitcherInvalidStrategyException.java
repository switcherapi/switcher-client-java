package com.switcher.client.exception;

public class SwitcherInvalidStrategyException extends Exception {
	
	private static final long serialVersionUID = -4091584736216245100L;

	public SwitcherInvalidStrategyException(final String strategyName) {
		
		super(String.format("Something went wrong: Invalid strategy %s", strategyName));
	}

}
