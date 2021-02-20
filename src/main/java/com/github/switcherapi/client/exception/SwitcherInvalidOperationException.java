package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherInvalidOperationException extends SwitcherException {
	
	private static final long serialVersionUID = 4685056886357966951L;

	public SwitcherInvalidOperationException(final String operation, final String strategyName) {
		
		super(String.format("Invalid operation %s for %s", operation, strategyName), null);
	}

}
