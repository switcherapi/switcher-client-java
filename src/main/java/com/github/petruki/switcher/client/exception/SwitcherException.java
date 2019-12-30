package com.github.petruki.switcher.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherException extends Exception {
	
	private static final long serialVersionUID = -1748896326811044977L;

	public SwitcherException(final String message, final Exception ex) {
		
		super(String.format("Something went wrong: %s", message), ex);
	}

}
