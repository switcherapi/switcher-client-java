package com.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2021-03-09
 */
public class SwitchersValidationException extends SwitcherException {

	public SwitchersValidationException(final String notFound) {
		super(String.format("Unable to load the following Switcher Key(s): %s", notFound), null);
	}
}
