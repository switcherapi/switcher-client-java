package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-07-26
 */
public class SwitcherInvalidNumericFormat extends SwitcherException {

	public SwitcherInvalidNumericFormat(final String input) {
		super(String.format("Numeric value is invalid for this operation. '%s'", input), null);
	}

}
