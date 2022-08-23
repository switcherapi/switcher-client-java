package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherException extends RuntimeException {

	public SwitcherException(final String message, final Exception ex) {
		super(String.format("Something went wrong: %s", message), ex);
	}

}
