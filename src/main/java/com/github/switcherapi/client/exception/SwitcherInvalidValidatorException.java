package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherInvalidValidatorException extends SwitcherException {

	public SwitcherInvalidValidatorException(final String strategyName) {
		super(String.format("Invalid validator class %s", strategyName), null);
	}

}
