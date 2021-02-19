package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-07-26
 */
public class SwitcherInvalidNumericFormat extends SwitcherException {
	
	private static final long serialVersionUID = -4906602391359654053L;

	public SwitcherInvalidNumericFormat(final String input) {
		
		super(String.format("Numeric value is invalid for this operation. '%s'", input), null);
	}

}
