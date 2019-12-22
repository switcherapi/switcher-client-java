package com.switcher.client.exception;

public class SwitcherInvalidOperationInputException extends Exception {
	
	private static final long serialVersionUID = 8792937418937916889L;

	public SwitcherInvalidOperationInputException(final String operation) {
		
		super(String.format("Something went wrong: Number of values received is not valid for [%s] operation", operation));
	}

}
