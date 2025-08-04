package com.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-07-26
 */
public class SwitcherInvalidDateTimeArgumentException extends SwitcherException {

	public SwitcherInvalidDateTimeArgumentException(final String value) {
		super(String.format("Something went wrong. It was not possible to convert this time duration %s", value), null);
	}

}
