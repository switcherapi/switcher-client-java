package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherInvalidOperationException extends SwitcherException {

	public SwitcherInvalidOperationException(final String operation, final String strategyName) {
		super(String.format("Invalid operation %s for %s", operation, strategyName), null);
	}

}
