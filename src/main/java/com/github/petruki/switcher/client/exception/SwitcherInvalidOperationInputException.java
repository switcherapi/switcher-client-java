package com.github.petruki.switcher.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherInvalidOperationInputException extends SwitcherException {
	
	private static final long serialVersionUID = 8792937418937916889L;

	public SwitcherInvalidOperationInputException(final String operation) {
		
		super(String.format("Number of values received is not valid for [%s] operation", operation), null);
	}

}
