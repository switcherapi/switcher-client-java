package com.switcher.client.exception;

public class SwitcherNoInputReceivedException extends Exception {
	
	private static final long serialVersionUID = -8414094976885969480L;

	public SwitcherNoInputReceivedException(final String strategyName) {
		
		super(String.format("Something went wrong: Couln't find any input for the strategy %s", strategyName));
	}

}
