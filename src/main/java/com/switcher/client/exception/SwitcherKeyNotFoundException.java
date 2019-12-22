package com.switcher.client.exception;

public class SwitcherKeyNotFoundException extends Exception {
	
	private static final long serialVersionUID = -6415733343415447201L;

	public SwitcherKeyNotFoundException(final String key) {
		
		super(String.format("Something went wrong: Unable to load a key %s", key));
	}

}
