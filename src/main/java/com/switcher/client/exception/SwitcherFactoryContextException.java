package com.switcher.client.exception;

public class SwitcherFactoryContextException extends Exception {
	
	private static final long serialVersionUID = -6340224967205872873L;

	public SwitcherFactoryContextException() {
		
		super("Context was not initialized. Call 'buildContext' to set up the factory");
	}

}
