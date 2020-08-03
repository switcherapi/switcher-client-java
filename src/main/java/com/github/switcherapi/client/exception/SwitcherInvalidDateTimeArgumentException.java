package com.github.switcherapi.client.exception;

/**
 * @author rogerio
 * @since 2020-07-26
 */
public class SwitcherInvalidDateTimeArgumentException extends SwitcherException {

	private static final long serialVersionUID = 6317212153071777960L;

	public SwitcherInvalidDateTimeArgumentException(final String value) {
		
		super(String.format("Something went wrong. It was not possible to convert this time duration %s", value), null);
	}

}
