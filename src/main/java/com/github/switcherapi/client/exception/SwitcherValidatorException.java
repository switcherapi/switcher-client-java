package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2023-02-17
 */
public class SwitcherValidatorException extends SwitcherException {

	public SwitcherValidatorException(final String input, final String value) {
		super(String.format("Failed to process input [%s] for [%s]", input, value), null);
	}

}
